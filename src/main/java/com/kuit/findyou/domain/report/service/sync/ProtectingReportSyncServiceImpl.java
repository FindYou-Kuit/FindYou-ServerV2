package com.kuit.findyou.domain.report.service.sync;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.dto.ReportWithImages;
import com.kuit.findyou.domain.report.dto.SyncResult;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient;
import com.kuit.findyou.global.external.client.ProtectingAnimalApiClient;
import com.kuit.findyou.global.external.dto.ProtectingAnimalItemDTO;
import com.kuit.findyou.global.external.util.ProtectingAnimalParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProtectingReportSyncServiceImpl implements ProtectingReportSyncService{

    private static final String DEFAULT_SIGNIFICANT = "미등록";

    private final ProtectingReportRepository protectingReportRepository;
    private final ReportImageRepository reportImageRepository;

    private final ProtectingAnimalApiClient protectingAnimalApiClient;
    private final KakaoCoordinateClient kakaoCoordinateClient;

    @Transactional
    @Override
    public void syncProtectingReports() {
        long startTime = System.currentTimeMillis();

        try {
            List<ProtectingAnimalItemDTO> apiItems = protectingAnimalApiClient.fetchAllProtectingAnimals();
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

        List<ProtectingReport> existingReports = protectingReportRepository.findAll();

        Set<String> existingNoticeNumbers = existingReports.stream()
                .map(ProtectingReport::getNoticeNumber)
                .collect(Collectors.toSet());

        List<ProtectingReport> reportsToDelete = findReportsToDelete(existingReports, apiNoticeNumbers);
        protectingReportRepository.deleteAll(reportsToDelete);

        List<ReportWithImages<ProtectingReport>> newReportBundles = createNewReports(apiItems, existingNoticeNumbers);

        // 1. report 먼저 저장
        List<ProtectingReport> newReports = newReportBundles.stream()
                .map(ReportWithImages::report)
                .toList();
        protectingReportRepository.saveAll(newReports);

        // 2. 연관관계 설정 후 이미지 저장
        List<ReportImage> allImages = new ArrayList<>();

        for (ReportWithImages<ProtectingReport> bundle : newReportBundles) {
            ProtectingReport report = bundle.report();
            for (ReportImage image : bundle.images()) {
                image.setReport(report);
                allImages.add(image);
            }
        }
        reportImageRepository.saveAll(allImages);

        return new SyncResult(reportsToDelete.size(), newReports.size());
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
                .species(ProtectingAnimalParser.parseSpecies(item.upKindNm()))
                .tag(ReportTag.PROTECTING)
                .date(ProtectingAnimalParser.parseDate(item.happenDt()))
                .address(item.careAddr())
                .latitude(coordinate.latitude())
                .longitude(coordinate.longitude())
                .user(null)
                .sex(ProtectingAnimalParser.parseSex(item.sexCd()))
                .age(ProtectingAnimalParser.parseAge(item.age()))
                .weight(ProtectingAnimalParser.parseWeight(item.weight()))
                .furColor(ProtectingAnimalParser.parseColor(item.colorCd()))
                .neutering(ProtectingAnimalParser.parseNeutering(item.neuterYn()))
                .significant(item.specialMark() != null ? item.specialMark() : DEFAULT_SIGNIFICANT)
                .foundLocation(item.happenPlace())
                .noticeNumber(item.noticeNo())
                .noticeStartDate(ProtectingAnimalParser.parseDate(item.noticeSdt()))
                .noticeEndDate(ProtectingAnimalParser.parseDate(item.noticeEdt()))
                .careName(item.careNm())
                .careTel(item.careTel())
                .authority(item.orgNm())
                .build();
    }

    private List<ReportWithImages<ProtectingReport>> createNewReports(List<ProtectingAnimalItemDTO> apiItems, Set<String> existingNoticeNumbers) {
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

                    return new ReportWithImages<>(report, images);
                })
                .toList();
    }


    private void logSyncResult(SyncResult result, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("DB 동기화 완료: 삭제된 데이터 = {}, 추가된 데이터 = {}, 소요 시간 = {}ms",
                result.deletedCount(), result.addedCount(), duration);
    }

}
