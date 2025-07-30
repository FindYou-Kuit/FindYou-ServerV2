package com.kuit.findyou.global.external.client;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.model.Neutering;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.Sex;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.global.external.dto.ProtectingAnimalApiFullResponse;
import com.kuit.findyou.global.external.dto.ProtectingAnimalItemDTO;
import com.kuit.findyou.global.external.properties.ProtectingAnimalApiProperties;
import com.kuit.findyou.global.external.util.ProtectingAnimalParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProtectingAnimalApiClient {

    private static final int DEFAULT_PAGE_SIZE = 1000;
    private static final String API_ENDPOINT = "/abandonmentPublic_v2";
    private static final String DEFAULT_SIGNIFICANT = "미등록";

    private final ProtectingReportRepository protectingReportRepository;
    private final ReportImageRepository reportImageRepository;
    private final ProtectingAnimalApiProperties properties;
    private final RestClient protectingAnimalRestClient;
    private final KakaoCoordinateClient kakaoCoordinateClient;

    public ProtectingAnimalApiClient(
            ProtectingReportRepository protectingReportRepository,
            ReportImageRepository reportImageRepository,
            ProtectingAnimalApiProperties properties,
            KakaoCoordinateClient kakaoCoordinateClient,
            @Qualifier("protectingAnimalRestClient") RestClient protectingAnimalRestClient
    ) {
        this.protectingReportRepository = protectingReportRepository;
        this.reportImageRepository = reportImageRepository;
        this.properties = properties;
        this.kakaoCoordinateClient = kakaoCoordinateClient;
        this.protectingAnimalRestClient = protectingAnimalRestClient;
    }

    @Transactional
    public void storeProtectingReports() {
        long startTime = System.currentTimeMillis();

        try {
            List<ProtectingAnimalItemDTO> apiItems = fetchAllProtectingAnimals();
            SyncResult syncResult = synchronizeData(apiItems);
            logSyncResult(syncResult, startTime);
        } catch (Exception e) {
            log.error("[구조동물 데이터 동기화 실패]", e);
        }
    }

    private SyncResult synchronizeData(List<ProtectingAnimalItemDTO> apiItems) {
        Set<String> apiNoticeNumbers = apiItems.stream()
                .map(ProtectingAnimalItemDTO::noticeNo)
                .collect(Collectors.toSet());

        List<ProtectingReport> existingReports = protectingReportRepository.findByNoticeNumberIn(apiNoticeNumbers);
        Set<String> existingNoticeNumbers = existingReports.stream()
                .map(ProtectingReport::getNoticeNumber)
                .collect(Collectors.toSet());

        List<ProtectingReport> reportsToDelete = findReportsToDelete(existingReports, apiNoticeNumbers);
        protectingReportRepository.deleteAll(reportsToDelete);

        List<ReportWithImages> newReportBundles = createNewReports(apiItems, existingNoticeNumbers);

        // 1. report 먼저 저장
        List<ProtectingReport> newReports = newReportBundles.stream()
                .map(ReportWithImages::report)
                .toList();
        protectingReportRepository.saveAll(newReports);

        // 2. 연관관계 설정 후 이미지 저장
        List<ReportImage> allImages = new ArrayList<>();

        for (ReportWithImages bundle : newReportBundles) {
            ProtectingReport report = bundle.report();
            for (ReportImage image : bundle.images()) {
                image.setReport(report);
                allImages.add(image);
            }
        }
        reportImageRepository.saveAll(allImages);

        return new SyncResult(reportsToDelete.size(), newReports.size());
    }


    private List<ProtectingAnimalItemDTO> fetchAllProtectingAnimals() {
        List<ProtectingAnimalItemDTO> allItems = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            try {
                ProtectingAnimalApiFullResponse response = fetchPageData(pageNo);

                if (isEmptyResponse(response)) {
                    log.warn("[구조동물 공공데이터 응답이 비어있습니다] pageNo={}", pageNo);
                    break;
                }

                List<ProtectingAnimalItemDTO> currentPageItems = response.response().body().items().item();
                allItems.addAll(currentPageItems);

                if (isLastPage(response, pageNo)) {
                    break;
                }

                pageNo++;

            } catch (Exception e) {
                log.error("[구조동물 공공데이터 페이지 {} 조회 실패]", pageNo, e);
                break;
            }
        }

        log.info("[구조동물 공공데이터 전체 조회 완료] 총 {}건", allItems.size());
        return allItems;
    }

    private ProtectingAnimalApiFullResponse fetchPageData(int pageNo) {
        return protectingAnimalRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_ENDPOINT)
                        .queryParam("serviceKey", properties.apiKey())
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", DEFAULT_PAGE_SIZE)
                        .queryParam("_type", "json")
                        .build())
                .header("Accept", "application/json")
                .retrieve()
                .body(ProtectingAnimalApiFullResponse.class);
    }

    private boolean isEmptyResponse(ProtectingAnimalApiFullResponse response) {
        return response == null ||
                response.response() == null ||
                response.response().body() == null ||
                response.response().body().items() == null ||
                response.response().body().items().item() == null;
    }

    private boolean isLastPage(ProtectingAnimalApiFullResponse response, int currentPage) {
        int totalCount = Integer.parseInt(response.response().body().totalCount());
        int totalPages = (int) Math.ceil((double) totalCount / DEFAULT_PAGE_SIZE);

        return currentPage >= totalPages;
    }

    private List<ProtectingReport> findReportsToDelete(List<ProtectingReport> existingReports, Set<String> apiNoticeNumbers) {
        return existingReports.stream()
                .filter(report -> !apiNoticeNumbers.contains(report.getNoticeNumber()))
                .toList();
    }

    private ProtectingReport convertToProtectingReport(ProtectingAnimalItemDTO item) {

        KakaoCoordinateClient.Coordinate coordinate = kakaoCoordinateClient.requestCoordinateOrDefault(item.careAddr());

        return ProtectingReport.builder()
                .breed(item.kindNm())
                .species(item.upKindNm())
                .tag(ReportTag.PROTECTING)
                .date(ProtectingAnimalParser.changeToLocalDate(item.happenDt()))
                .address(item.careAddr())
                .latitude(coordinate.latitude())
                .longitude(coordinate.longitude())
                .user(null)
                .sex(Sex.valueOf(item.sexCd()))
                .age(ProtectingAnimalParser.parseAge(item.age()))
                .weight(ProtectingAnimalParser.parseWeight(item.weight()))
                .furColor(ProtectingAnimalParser.parseColor(item.colorCd()))
                .neutering(Neutering.valueOf(item.neuterYn()))
                .significant(item.specialMark() != null ? item.specialMark() : DEFAULT_SIGNIFICANT)
                .foundLocation(item.happenPlace())
                .noticeNumber(item.noticeNo())
                .noticeStartDate(ProtectingAnimalParser.changeToLocalDate(item.noticeSdt()))
                .noticeEndDate(ProtectingAnimalParser.changeToLocalDate(item.noticeEdt()))
                .careName(item.careNm())
                .careTel(item.careTel())
                .authority(item.orgNm())
                .build();
    }

    private List<ReportWithImages> createNewReports(List<ProtectingAnimalItemDTO> apiItems, Set<String> existingNoticeNumbers) {
        return apiItems.stream()
                .filter(item -> !existingNoticeNumbers.contains(item.noticeNo()))
                .map(item -> {
                    ProtectingReport report = convertToProtectingReport(item);
                    List<ReportImage> images = new ArrayList<>();

                    if (item.popfile1() != null && !item.popfile1().isBlank()) {
                        images.add(ReportImage.createReportImage(item.popfile1(), UUID.randomUUID().toString()));
                    }
                    if (item.popfile2() != null && !item.popfile2().isBlank()) {
                        images.add(ReportImage.createReportImage(item.popfile2(), UUID.randomUUID().toString()));
                    }

                    return new ReportWithImages(report, images);
                })
                .toList();
    }


    private void logSyncResult(SyncResult result, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("DB 동기화 완료: 삭제된 데이터 = {}, 추가된 데이터 = {}, 소요 시간 = {}ms",
                result.deletedCount(), result.addedCount(), duration);
    }

    // 동기화 결과를 담는 레코드
    private record SyncResult(int deletedCount, int addedCount) {}

    private record ReportWithImages(ProtectingReport report, List<ReportImage> images) {}

}