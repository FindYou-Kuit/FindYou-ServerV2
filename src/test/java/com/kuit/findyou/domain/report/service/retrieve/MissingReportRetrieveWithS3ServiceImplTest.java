package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.MissingReportDetailStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
class MissingReportRetrieveWithS3ServiceImplTest {

    @Mock
    private MissingReportRepository missingReportRepository;


    @Mock
    private MissingReportDetailStrategy missingReportDetailStrategy;


    @InjectMocks
    private MissingReportRetrieveWithS3ServiceImpl missingReportRetrieveWithS3Service;

    @Test
    @DisplayName("어제 날짜 실종글이 없으면 빈 리스트 반환")
    void getRandomMissingReportsWithS3_whenNoReports_thenReturnEmpty() {
        // given
        when(missingReportRepository.findByDate(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        List<MissingReportDetailResponseDTO> result =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(3);

        // then
        assertThat(result).isEmpty();

        verify(missingReportRepository, times(1)).findByDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("요청 개수보다 실종글이 적으면 전체 개수만큼만 반환")
    void getRandomMissingReportsWithS3_whenCountGreaterThanSize_thenReturnAllReports() {
        // given
        MissingReport report1 = mock(MissingReport.class);
        MissingReport report2 = mock(MissingReport.class);

        when(report1.getReportImages()).thenReturn(Collections.<ReportImage>emptyList());
        when(report2.getReportImages()).thenReturn(Collections.<ReportImage>emptyList());

        when(missingReportRepository.findByDate(any(LocalDate.class)))
                .thenReturn(new ArrayList<>(List.of(report1, report2)));

        MissingReportDetailResponseDTO dto1 = mock(MissingReportDetailResponseDTO.class);
        MissingReportDetailResponseDTO dto2 = mock(MissingReportDetailResponseDTO.class);

        when(missingReportDetailStrategy.toDetailDto(eq(report1), anyList(), eq(false))).thenReturn(dto1);
        when(missingReportDetailStrategy.toDetailDto(eq(report2), anyList(), eq(false))).thenReturn(dto2);

        // when
        List<MissingReportDetailResponseDTO> result =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(10);

        // then
        assertThat(result).hasSize(2).containsExactlyInAnyOrder(dto1, dto2);

        verify(missingReportRepository, times(1)).findByDate(any(LocalDate.class));
        verify(missingReportDetailStrategy, times(2))
                .toDetailDto(any(MissingReport.class), anyList(), eq(false));

    }

    @Test
    @DisplayName("요청 개수가 전체 개수보다 적으면 요청 개수만큼만 반환")
    void getRandomMissingReportsWithS3_whenCountLessThanSize_thenReturnCountReports() {
        // given
        MissingReport r1 = mock(MissingReport.class);
        MissingReport r2 = mock(MissingReport.class);
        MissingReport r3 = mock(MissingReport.class);

        when(r1.getReportImages()).thenReturn(Collections.emptyList());
        when(r2.getReportImages()).thenReturn(Collections.emptyList());
        when(r3.getReportImages()).thenReturn(Collections.emptyList());

        when(missingReportRepository.findByDate(any(LocalDate.class)))
                .thenReturn(new ArrayList<>(List.of(r1, r2, r3)));

        when(missingReportDetailStrategy.toDetailDto(any(), anyList(), eq(false)))
                .thenReturn(mock(MissingReportDetailResponseDTO.class));

        // when
        List<MissingReportDetailResponseDTO> result =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(2);

        // then
        assertThat(result).hasSize(2);

        verify(missingReportRepository, times(1)).findByDate(any(LocalDate.class));
        verify(missingReportDetailStrategy, times(2))
                .toDetailDto(any(MissingReport.class), anyList(), eq(false));
    }

    @Test
    @DisplayName("Repository 조회 날짜가 '어제'로 들어간다")
    void getRandomMissingReportsWithS3_verifyRepositoryDateIsYesterday() {
        // given
        when(missingReportRepository.findByDate(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        // when
        missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(1);

        // then
        verify(missingReportRepository).findByDate(dateCaptor.capture());
        assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now().minusDays(1));
    }
    @Test
    @DisplayName("DB에 저장된 imageUrl을 그대로 DTO에 전달한다")
    void getRandomMissingReportsWithS3_returnsStoredImageUrls() {
        MissingReport report = mock(MissingReport.class);

        ReportImage img1 = mock(ReportImage.class);
        when(img1.getImageUrl()).thenReturn("https://cdn.findyou.store/a.jpg");

        ReportImage img2 = mock(ReportImage.class);
        when(img2.getImageUrl()).thenReturn("https://cdn.findyou.store/b.jpg");

        when(report.getReportImages()).thenReturn(List.of(img1, img2));
        when(missingReportRepository.findByDate(any()))
                .thenReturn(List.of(report));

        MissingReportDetailResponseDTO dto = mock(MissingReportDetailResponseDTO.class);

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        when(missingReportDetailStrategy.toDetailDto(eq(report), captor.capture(), eq(false)))
                .thenReturn(dto);

        List<MissingReportDetailResponseDTO> result =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(1);

        assertThat(result).hasSize(1);
        assertThat(captor.getValue())
                .containsExactly(
                        "https://cdn.findyou.store/a.jpg",
                        "https://cdn.findyou.store/b.jpg"
                );
    }
    @Test
    @DisplayName("count가 0 이하이면 최소 1개를 반환한다")
    void getRandomMissingReportsWithS3_whenCountZeroOrNegative_thenReturnAtLeastOne() {
        // given
        MissingReport r1 = mock(MissingReport.class);
        MissingReport r2 = mock(MissingReport.class);
        MissingReport r3 = mock(MissingReport.class);

        when(r1.getReportImages()).thenReturn(Collections.emptyList());
        when(r2.getReportImages()).thenReturn(Collections.emptyList());
        when(r3.getReportImages()).thenReturn(Collections.emptyList());

        when(missingReportRepository.findByDate(any(LocalDate.class)))
                .thenReturn(new ArrayList<>(List.of(r1, r2, r3)));

        when(missingReportDetailStrategy.toDetailDto(any(MissingReport.class), anyList(), eq(false)))
                .thenReturn(mock(MissingReportDetailResponseDTO.class));

        // when
        List<MissingReportDetailResponseDTO> resultZero =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(0);

        List<MissingReportDetailResponseDTO> resultNegative =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(-5);

        // then
        assertThat(resultZero).hasSize(1);
        assertThat(resultNegative).hasSize(1);

        verify(missingReportRepository, times(2)).findByDate(any(LocalDate.class));
        verify(missingReportDetailStrategy, times(2))
                .toDetailDto(any(MissingReport.class), anyList(), eq(false));
    }

    @Test
    @DisplayName("imageUrl에서 null/blank/중복 제거 후 DTO로 전달한다")
    void getRandomMissingReportsWithS3_filtersNullBlankAndDistinctImageUrls() {
        // given
        MissingReport report = mock(MissingReport.class);

        ReportImage imgNull = mock(ReportImage.class);
        when(imgNull.getImageUrl()).thenReturn(null);

        ReportImage imgBlank = mock(ReportImage.class);
        when(imgBlank.getImageUrl()).thenReturn("   ");

        ReportImage imgA1 = mock(ReportImage.class);
        when(imgA1.getImageUrl()).thenReturn("https://cdn.findyou.store/a.jpg");

        ReportImage imgA2 = mock(ReportImage.class);
        when(imgA2.getImageUrl()).thenReturn("https://cdn.findyou.store/a.jpg");

        ReportImage imgB = mock(ReportImage.class);
        when(imgB.getImageUrl()).thenReturn("https://cdn.findyou.store/b.jpg");

        when(report.getReportImages()).thenReturn(List.of(imgNull, imgBlank, imgA1, imgA2, imgB));

        when(missingReportRepository.findByDate(any(LocalDate.class)))
                .thenReturn(new ArrayList<>(List.of(report)));

        MissingReportDetailResponseDTO dto = mock(MissingReportDetailResponseDTO.class);

        ArgumentCaptor<List<String>> urlCaptor = ArgumentCaptor.forClass(List.class);
        when(missingReportDetailStrategy.toDetailDto(eq(report), urlCaptor.capture(), eq(false)))
                .thenReturn(dto);

        // when
        List<MissingReportDetailResponseDTO> result =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(1);

        // then
        assertThat(result).hasSize(1).containsExactly(dto);
        assertThat(urlCaptor.getValue())
                .containsExactly(
                        "https://cdn.findyou.store/a.jpg",
                        "https://cdn.findyou.store/b.jpg"
                );

        verify(missingReportRepository, times(1)).findByDate(any(LocalDate.class));
        verify(missingReportDetailStrategy, times(1))
                .toDetailDto(eq(report), anyList(), eq(false));
    }

}
