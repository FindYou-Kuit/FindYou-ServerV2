package com.kuit.findyou.domain.report.service.sync;

import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient;
import com.kuit.findyou.global.external.client.MissingAnimalApiClient;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient.Coordinate;
import com.kuit.findyou.global.external.dto.MissingAnimalItemDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissingReportSyncServiceImplTest {

    @Mock
    MissingReportRepository missingReportRepository;
    @Mock
    ReportImageRepository reportImageRepository;
    @Mock
    BreedRepository breedRepository;
    @Mock
    MissingAnimalApiClient missingAnimalApiClient;
    @Mock
    KakaoCoordinateClient kakaoCoordinateClient;

    @InjectMocks
    MissingReportSyncServiceImpl service;

    /**
     * 실제로 변환까지 갈 아이템 (필요한 getter 모두 스텁)
     */
    private MissingAnimalItemDTO fullItem(String url, String addr) {
        MissingAnimalItemDTO dto = mock(MissingAnimalItemDTO.class);
        when(dto.popfile()).thenReturn(url);
        when(dto.happenAddr()).thenReturn(addr);
        when(dto.kindCd()).thenReturn("진돗개");
        when(dto.happenDt()).thenReturn("2024-07-18 13:45:12.3");
        when(dto.sexCd()).thenReturn("M");
        when(dto.rfidCd()).thenReturn("RFID");
        when(dto.age()).thenReturn("3");
        when(dto.colorCd()).thenReturn("갈색");
        when(dto.specialMark()).thenReturn("등에 점 2개");
        when(dto.callName()).thenReturn("홍길동");
        when(dto.callTel()).thenReturn("010-1234-5678");
        when(dto.happenPlace()).thenReturn("강남구역삼동");
        return dto;
    }

    /**
     * 중복으로 스킵될 아이템 (popfile 만 필요)
     */
    private MissingAnimalItemDTO dupItem(String url) {
        MissingAnimalItemDTO dto = mock(MissingAnimalItemDTO.class);
        when(dto.popfile()).thenReturn(url);
        return dto;
    }

    /**
     * 품종 레포는 항상 조회되므로 공통 스텁
     */
    private void stubEmptyBreeds() {
        when(breedRepository.findAllDogBreeds()).thenReturn(Collections.emptyList());
        when(breedRepository.findAllCatBreeds()).thenReturn(Collections.emptyList());
        when(breedRepository.findAllEtcBreeds()).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("신규 3건(그중 1건은 이미지 없음) → 보고서 3건 저장, 이미지 2건 저장")
    void sync_success_savesReportsAndImages() {
        stubEmptyBreeds();
        when(reportImageRepository.findAllImageUrlsForMissing()).thenReturn(Set.of());

        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new Coordinate(new BigDecimal("37.10"), new BigDecimal("127.20")));

        MissingAnimalItemDTO a = fullItem("a.jpg", "서울 강남");
        MissingAnimalItemDTO noImg = fullItem(null, "서울 서초");     // 이미지 없음
        MissingAnimalItemDTO b = fullItem("b.jpg", "서울 송파");

        when(missingAnimalApiClient.fetchAllMissingAnimals(anyString(), anyString()))
                .thenReturn(List.of(a, noImg, b));

        service.syncMissingReports();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MissingReport>> reportCap = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReportImage>> imageCap = ArgumentCaptor.forClass(List.class);

        verify(missingReportRepository).saveAll(reportCap.capture());
        verify(reportImageRepository).saveAll(imageCap.capture());

        assertThat(reportCap.getValue()).hasSize(3);
        assertThat(imageCap.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("기존 이미지 URL이 포함된 항목은 스킵된다 (dupe.jpg는 제외)")
    void sync_skipsDuplicateImageItems() {
        stubEmptyBreeds();
        when(reportImageRepository.findAllImageUrlsForMissing()).thenReturn(Set.of("dupe.jpg"));

        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new Coordinate(new BigDecimal("37.10"), new BigDecimal("127.20")));

        MissingAnimalItemDTO dupe = dupItem("dupe.jpg");              // 스킵됨 → popfile 외 스텁 금지
        MissingAnimalItemDTO ok = fullItem("ok.jpg", "서울 금천");

        when(missingAnimalApiClient.fetchAllMissingAnimals(anyString(), anyString()))
                .thenReturn(List.of(dupe, ok));

        service.syncMissingReports();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MissingReport>> reportCap = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReportImage>> imageCap = ArgumentCaptor.forClass(List.class);

        verify(missingReportRepository).saveAll(reportCap.capture());
        verify(reportImageRepository).saveAll(imageCap.capture());

        assertThat(reportCap.getValue()).hasSize(1);
        assertThat(imageCap.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("모든 항목이 기존 이미지 URL과 중복이면 아무 것도 저장하지 않는다")
    void sync_allDuplicates_doNothing() {
        stubEmptyBreeds();
        when(reportImageRepository.findAllImageUrlsForMissing()).thenReturn(Set.of("a.jpg", "b.jpg"));

        MissingAnimalItemDTO a = dupItem("a.jpg"); // 전부 스킵 경로 → popfile 만
        MissingAnimalItemDTO b = dupItem("b.jpg");

        when(missingAnimalApiClient.fetchAllMissingAnimals(anyString(), anyString()))
                .thenReturn(List.of(a, b));

        service.syncMissingReports();

        verify(missingReportRepository, never()).saveAll(anyList());
        verify(reportImageRepository, never()).saveAll(anyList());
        verifyNoInteractions(kakaoCoordinateClient);
    }
}

