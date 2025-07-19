package com.kuit.findyou.global.common.external.client;

import com.kuit.findyou.domain.report.model.Neutering;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.Sex;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.global.common.external.dto.ProtectingAnimalApiFullResponse;
import com.kuit.findyou.global.common.external.dto.ProtectingAnimalItemDTO;
import com.kuit.findyou.global.common.external.properties.ProtectingAnimalApiProperties;
import com.kuit.findyou.global.common.external.util.ProtectingAnimalParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProtectingAnimalApiClient {

    private static final int DEFAULT_PAGE_SIZE = 1000;
    private static final String API_ENDPOINT = "/abandonmentPublic_v2";
    private static final String DEFAULT_SIGNIFICANT = "미등록";

    private final ProtectingReportRepository protectingReportRepository;
    private final ProtectingAnimalApiProperties properties;
    private final RestClient protectingAnimalRestClient;

    public ProtectingAnimalApiClient(
            ProtectingReportRepository protectingReportRepository,
            ProtectingAnimalApiProperties properties,
            @Qualifier("protectingAnimalRestClient") RestClient protectingAnimalRestClient
    ) {
        this.protectingReportRepository = protectingReportRepository;
        this.properties = properties;
        this.protectingAnimalRestClient = protectingAnimalRestClient;
    }

    @Transactional
    public void storeProtectingReports() {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 외부 API에서 모든 데이터 조회
            List<ProtectingAnimalItemDTO> apiItems = fetchAllProtectingAnimals();

            // 2. 데이터 동기화 수행
            SyncResult syncResult = synchronizeData(apiItems);

            // 3. 결과 로깅
            logSyncResult(syncResult, startTime);

        } catch (Exception e) {
            log.error("[구조동물 데이터 동기화 실패]", e);
            throw new RuntimeException("구조동물 데이터 동기화 중 오류가 발생했습니다.", e);
        }
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

    private SyncResult synchronizeData(List<ProtectingAnimalItemDTO> apiItems) {
        // 1. API 데이터의 공지번호 추출
        Set<String> apiNoticeNumbers = apiItems.stream()
                .map(ProtectingAnimalItemDTO::noticeNo)
                .collect(Collectors.toSet());

        // 2. 기존 데이터 조회
        List<ProtectingReport> existingReports = protectingReportRepository.findByNoticeNumberIn(apiNoticeNumbers);
        Set<String> existingNoticeNumbers = existingReports.stream()
                .map(ProtectingReport::getNoticeNumber)
                .collect(Collectors.toSet());

        // 3. 삭제할 데이터 처리 (DB 에는 있지만 API 에는 없는 데이터)
        List<ProtectingReport> reportsToDelete = findReportsToDelete(existingReports, apiNoticeNumbers);
        protectingReportRepository.deleteAll(reportsToDelete);

        // 4. 새로 추가할 데이터 처리 (API 에는 있지만 DB 에는 없는 데이터)
        List<ProtectingReport> newReports = createNewReports(apiItems, existingNoticeNumbers);
        protectingReportRepository.saveAll(newReports);

        return new SyncResult(reportsToDelete.size(), newReports.size());
    }

    private List<ProtectingReport> findReportsToDelete(List<ProtectingReport> existingReports, Set<String> apiNoticeNumbers) {
        return existingReports.stream()
                .filter(report -> !apiNoticeNumbers.contains(report.getNoticeNumber()))
                .toList();
    }

    private List<ProtectingReport> createNewReports(List<ProtectingAnimalItemDTO> apiItems, Set<String> existingNoticeNumbers) {
        return apiItems.stream()
                .filter(item -> !existingNoticeNumbers.contains(item.noticeNo()))
                .map(this::convertToProtectingReport)
                .toList();
    }

    private ProtectingReport convertToProtectingReport(ProtectingAnimalItemDTO item) {

        return ProtectingReport.builder()
                .breed(item.upKindNm())
                .species(item.kindNm())
                .tag(ReportTag.PROTECTING)
                .date(ProtectingAnimalParser.changeToLocalDate(item.happenDt()))
                .address(item.careAddr())
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

    private void logSyncResult(SyncResult result, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("DB 동기화 완료: 삭제된 데이터 = {}, 추가된 데이터 = {}, 소요 시간 = {}ms",
                result.deletedCount(), result.addedCount(), duration);
    }

    // 동기화 결과를 담는 레코드
    private record SyncResult(int deletedCount, int addedCount) {}
}