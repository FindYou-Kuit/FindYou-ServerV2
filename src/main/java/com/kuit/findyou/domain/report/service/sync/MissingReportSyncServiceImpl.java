package com.kuit.findyou.domain.report.service.sync;

import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.dto.ReportWithImages;
import com.kuit.findyou.domain.report.dto.SyncResult;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient;
import com.kuit.findyou.global.external.client.MissingAnimalApiClient;
import com.kuit.findyou.global.external.dto.MissingAnimalItemDTO;
import com.kuit.findyou.global.external.util.MissingAnimalParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.MISSING_REPORT_SYNC_FAILED;

@Slf4j
@RequiredArgsConstructor
@Service
public class MissingReportSyncServiceImpl implements MissingReportSyncService {

    private final MissingReportRepository missingReportRepository;
    private final ReportImageRepository reportImageRepository;
    private final BreedRepository breedRepository;
    private final MissingAnimalApiClient missingAnimalApiClient;
    private final KakaoCoordinateClient kakaoCoordinateClient;

    @Transactional
    @Override
    public void syncMissingReports() {
        long startTime = System.currentTimeMillis();

        try {
            // Breed 데이터를 미리 조회
            Set<String> dogBreeds = getDogBreeds();
            Set<String> catBreeds = getCatBreeds();
            Set<String> etcBreeds = getEtcBreeds();

            String targetDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);

            List<MissingAnimalItemDTO> apiItems = missingAnimalApiClient.fetchAllMissingAnimals(targetDate, targetDate);
            SyncResult syncResult = synchronizeData(apiItems, dogBreeds, catBreeds, etcBreeds);
            logSyncResult(syncResult, startTime);
        } catch (Exception e) {
            log.error("[분실 동물 데이터 동기화 실패]", e);
            throw new CustomException(MISSING_REPORT_SYNC_FAILED);
        }
    }

    private SyncResult synchronizeData(List<MissingAnimalItemDTO> apiItems,
                                       Set<String> dogBreeds,
                                       Set<String> catBreeds,
                                       Set<String> otherBreeds) {
        // 기존 DB의 모든 이미지 URL 을 가져와서 중복 체크용으로 사용
        Set<String> existingMissingReportImageUrls = getAllExistingMissingReportImageUrls();

        // 중복되지 않는 새로운 데이터만 필터링
        List<ReportWithImages<MissingReport>> newReportBundles = createNewReports(apiItems, existingMissingReportImageUrls, dogBreeds, catBreeds, otherBreeds);

        if (newReportBundles.isEmpty()) {
            log.info("[분실 동물 데이터 동기화] 새로운 데이터가 없습니다.");
            return new SyncResult(0, 0);
        }

        // 1. report 먼저 저장
        List<MissingReport> newReports = newReportBundles.stream()
                .map(ReportWithImages::report)
                .toList();
        missingReportRepository.saveAll(newReports);

        // 2. 연관관계 설정 후 이미지 저장
        List<ReportImage> allImages = new ArrayList<>();
        for (ReportWithImages<MissingReport> bundle : newReportBundles) {
            MissingReport report = bundle.report();
            for (ReportImage image : bundle.images()) {
                image.setReport(report);
                allImages.add(image);
            }
        }
        reportImageRepository.saveAll(allImages);

        return new SyncResult(0, newReports.size());
    }

    private Set<String> getAllExistingMissingReportImageUrls() {
        // 기존 DB의 모든 실종신고글의 이미지 URL 을 가져와 중복 체크용으로 사용
        return reportImageRepository.findAllImageUrlsForMissing();
    }

    private MissingReport convertToMissingReport(MissingAnimalItemDTO item,
                                                 Set<String> dogBreeds,
                                                 Set<String> catBreeds,
                                                 Set<String> otherBreeds) {
        KakaoCoordinateClient.Coordinate coordinate = kakaoCoordinateClient.requestCoordinateOrDefault(item.happenAddr());

        String breedName = MissingAnimalParser.parseBreed(item.kindCd());

        return MissingReport.builder()
                .breed(MissingAnimalParser.trimOrNull(breedName))
                .species(MissingAnimalParser.parseSpecies(breedName, dogBreeds, catBreeds, otherBreeds))
                .tag(ReportTag.MISSING)
                .date(MissingAnimalParser.parseDate(item.happenDt()))
                .address(MissingAnimalParser.trimOrNull(item.happenAddr()))
                .latitude(coordinate.latitude())
                .longitude(coordinate.longitude())
                .user(null)
                .sex(MissingAnimalParser.parseSex(item.sexCd()))
                .rfid(MissingAnimalParser.trimOrNull(item.rfidCd()))
                .age(MissingAnimalParser.trimOrNull(item.age()))
                .furColor(MissingAnimalParser.trimOrNull(item.colorCd()))
                .significant(MissingAnimalParser.parseSignificant(item.specialMark()))
                .reporterName(MissingAnimalParser.trimOrNull(item.callName()))
                .reporterTel(MissingAnimalParser.trimOrNull(item.callTel()))
                .landmark(MissingAnimalParser.trimOrNull(item.happenPlace()))
                .build();
    }

    private List<ReportWithImages<MissingReport>> createNewReports(List<MissingAnimalItemDTO> apiItems,
                                                                   Set<String> existingImageUrls,
                                                                   Set<String> dogBreeds,
                                                                   Set<String> catBreeds,
                                                                   Set<String> otherBreeds) {
        return apiItems.stream()
                .map(item -> {
                    String url = item.popfile();

                    // 1. 이미지 중복이면 글 생성도 스킵
                    if (url != null && existingImageUrls.contains(url)) {
                        return null; // 나중에 filter 로 제거
                    }

                    // 2. 글 생성
                    MissingReport report = convertToMissingReport(item, dogBreeds, catBreeds, otherBreeds);

                    // 3. 이미지 조건부 추가 (null/blank 면 이미지 없이 글만)
                    List<ReportImage> images = new ArrayList<>();

                    if (url != null) {
                        images.add(ReportImage.createReportImage(url, report));
                    }

                    return new ReportWithImages<>(report, images);
                })
                .filter(Objects::nonNull) // 중복 이미지였던 항목 제거
                .toList();
    }

    // Breed 데이터 조회 메서드들
    private Set<String> getDogBreeds() {
        return breedRepository.findAllDogBreeds().stream()
                .map(Breed::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> getCatBreeds() {
        return breedRepository.findAllCatBreeds().stream()
                .map(Breed::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> getEtcBreeds() {
        return breedRepository.findAllEtcBreeds().stream()
                .map(Breed::getName)
                .collect(Collectors.toSet());
    }

    private void logSyncResult(SyncResult result, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("분실 동물 DB 동기화 완료: 추가된 데이터 = {}, 소요 시간 = {}ms",
                result.addedCount(), duration);
    }
}