package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.MissingReportDetailStrategy;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
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
import org.springframework.web.client.RestTemplate;

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
    private ImageUploader imageUploader;

    @Mock
    private MissingReportDetailStrategy missingReportDetailStrategy;

    @Mock
    private RestTemplate restTemplate;

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
        verifyNoInteractions(imageUploader, missingReportDetailStrategy);
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

        verifyNoInteractions(imageUploader);
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
    @DisplayName("이미지 다운로드에서 예외가 나도 전체 API는 실패하지 않고 DTO는 반환")
    void getRandomMissingReportsWithS3_whenUnexpectedException_thenContinue() {
        // given
        MissingReport report = mock(MissingReport.class);
        when(report.getId()).thenReturn(1L);

        ReportImage img1 = mock(ReportImage.class);
        when(img1.getImageUrl()).thenReturn("http://localhost:65535/nonexistent");
        when(report.getReportImages()).thenReturn(List.of(img1));

        when(missingReportRepository.findByDate(any(LocalDate.class)))
                .thenReturn(new ArrayList<>(List.of(report)));

        MissingReportDetailResponseDTO dto = mock(MissingReportDetailResponseDTO.class);
        when(missingReportDetailStrategy.toDetailDto(eq(report), anyList(), eq(false))).thenReturn(dto);

        // RestTemplate이 예외 던지게(다운로드 실패)
        when(restTemplate.getForObject(anyString(), eq(byte[].class)))
                .thenThrow(new RuntimeException("다운로드 오류"));

        // when
        List<MissingReportDetailResponseDTO> result =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(1);

        // then
        assertThat(result).hasSize(1).containsExactly(dto);
    }

    @Test
    @DisplayName("이미지를 정상적으로 받으면 S3 업로드되고 업로드된 URL 리스트가 DTO 생성에 전달된다")
    void getRandomMissingReportsWithS3_successUploadFlow() {
        // given
        MissingReport report = mock(MissingReport.class);
        when(report.getId()).thenReturn(100L);

        ReportImage img1 = mock(ReportImage.class);
        ReportImage img2 = mock(ReportImage.class);

        when(img1.getImageUrl()).thenReturn("http://example.com/1.jpg");
        when(img2.getImageUrl()).thenReturn("http://example.com/2.jpg");
        when(report.getReportImages()).thenReturn(List.of(img1, img2));

        when(missingReportRepository.findByDate(any(LocalDate.class)))
                .thenReturn(new ArrayList<>(List.of(report)));

        when(restTemplate.getForObject(eq("http://example.com/1.jpg"), eq(byte[].class)))
                .thenReturn(new byte[]{1, 2, 3});
        when(restTemplate.getForObject(eq("http://example.com/2.jpg"), eq(byte[].class)))
                .thenReturn(new byte[]{4, 5, 6});

        when(imageUploader.upload(any(byte[].class), anyString(), eq("image/jpeg")))
                .thenReturn("https://s3.findyou.store/1.jpg", "https://s3.findyou.store/2.jpg");

        MissingReportDetailResponseDTO dto = mock(MissingReportDetailResponseDTO.class);

        ArgumentCaptor<List<String>> s3UrlsCaptor = ArgumentCaptor.forClass(List.class);
        when(missingReportDetailStrategy.toDetailDto(eq(report), s3UrlsCaptor.capture(), eq(false)))
                .thenReturn(dto);

        // when
        List<MissingReportDetailResponseDTO> result =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(1);

        // then
        assertThat(result).hasSize(1).containsExactly(dto);

        List<String> capturedUrls = s3UrlsCaptor.getValue();
        assertThat(capturedUrls).containsExactly(
                "https://s3.findyou.store/1.jpg",
                "https://s3.findyou.store/2.jpg"
        );

        verify(restTemplate, times(1)).getForObject("http://example.com/1.jpg", byte[].class);
        verify(restTemplate, times(1)).getForObject("http://example.com/2.jpg", byte[].class);
        verify(imageUploader, times(2)).upload(any(byte[].class), anyString(), eq("image/jpeg"));
    }

    @Test
    @DisplayName("S3 업로드/다운로드에서 예외가 발생해도 DTO는 정상 반환되고, 성공한 URL만 전달된다")
    void getRandomMissingReportsWithS3_whenUploadFails_thenContinuePerImage() {
        // given
        MissingReport report = mock(MissingReport.class);
        when(report.getId()).thenReturn(200L);

        ReportImage img1 = mock(ReportImage.class);
        ReportImage img2 = mock(ReportImage.class);

        when(img1.getImageUrl()).thenReturn("http://example.com/1.jpg");
        when(img2.getImageUrl()).thenReturn("http://example.com/2.jpg");
        when(report.getReportImages()).thenReturn(List.of(img1, img2));

        when(missingReportRepository.findByDate(any(LocalDate.class)))
                .thenReturn(new ArrayList<>(List.of(report)));

        // 1번: 다운로드 성공 -> 업로드 실패
        when(restTemplate.getForObject("http://example.com/1.jpg", byte[].class))
                .thenReturn(new byte[]{1, 2, 3});
        when(imageUploader.upload(any(byte[].class), anyString(), eq("image/jpeg")))
                .thenThrow(new FileUploadingFailedException("업로드 실패"));

        // 2번: 다운로드 자체 실패
        when(restTemplate.getForObject("http://example.com/2.jpg", byte[].class))
                .thenThrow(new RuntimeException("다운로드 오류"));

        MissingReportDetailResponseDTO dto = mock(MissingReportDetailResponseDTO.class);

        ArgumentCaptor<List<String>> s3UrlsCaptor = ArgumentCaptor.forClass(List.class);
        when(missingReportDetailStrategy.toDetailDto(eq(report), s3UrlsCaptor.capture(), eq(false)))
                .thenReturn(dto);

        // when
        List<MissingReportDetailResponseDTO> result =
                missingReportRetrieveWithS3Service.getRandomMissingReportsWithS3(1);

        // then
        assertThat(result).hasSize(1).containsExactly(dto);

        // 둘 다 실패했으니 URL은 빈 리스트
        assertThat(s3UrlsCaptor.getValue()).isEmpty();

        verify(restTemplate, times(1)).getForObject("http://example.com/1.jpg", byte[].class);
        verify(restTemplate, times(1)).getForObject("http://example.com/2.jpg", byte[].class);
        verify(imageUploader, times(1)).upload(any(byte[].class), anyString(), eq("image/jpeg"));
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
}
