package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.ProtectingReportDetailStrategy;
import com.kuit.findyou.global.common.exception.CustomException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProtectingReportRetrieveWithS3ServiceImplTest {

    @Mock
    private ProtectingReportRepository protectingReportRepository;

    @Mock
    private ImageUploader imageUploader;

    @Mock
    private ProtectingReportDetailStrategy protectingReportDetailStrategy;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProtectingReportRetrieveWithS3ServiceImpl protectingReportRetrieveWithS3Service;

    @Test
    @DisplayName("보호글이 하나도 없으면 PROTECTING_REPORT_NOT_FOUND 예외")
    void getRandomProtectingReportsWithS3_whenNoReports_thenThrow() {
        // given
        when(protectingReportRepository.findRandomReports(any(Pageable.class))).thenReturn(List.of());

        // when & then
        assertThrows(CustomException.class,
                () -> protectingReportRetrieveWithS3Service.getRandomProtectingReportsWithS3(3)
        );

        verify(protectingReportRepository, times(1)).findRandomReports(any(Pageable.class));
        verifyNoInteractions(imageUploader, protectingReportDetailStrategy);
    }

    @Test
    @DisplayName("요청 개수보다 보호글이 적으면 전체 개수만큼만 DTO를 반환한다")
    void getRandomProtectingReportsWithS3_whenCountGreaterThanSize_thenReturnAllReports() {
        // given
        ProtectingReport report1 = mock(ProtectingReport.class);
        ProtectingReport report2 = mock(ProtectingReport.class);

        // 이미지 없다고 가정
        when(report1.getReportImages()).thenReturn(Collections.<ReportImage>emptyList());
        when(report2.getReportImages()).thenReturn(Collections.<ReportImage>emptyList());

        when(protectingReportRepository.findRandomReports(any(Pageable.class))).thenReturn(List.of(report1, report2));

        ProtectingReportDetailResponseDTO dto1 = mock(ProtectingReportDetailResponseDTO.class);
        ProtectingReportDetailResponseDTO dto2 = mock(ProtectingReportDetailResponseDTO.class);

        when(protectingReportDetailStrategy.toDetailDto(eq(report1), anyList(), eq(false)))
                .thenReturn(dto1);
        when(protectingReportDetailStrategy.toDetailDto(eq(report2), anyList(), eq(false)))
                .thenReturn(dto2);

        // when
        List<ProtectingReportDetailResponseDTO> result =
                protectingReportRetrieveWithS3Service.getRandomProtectingReportsWithS3(10);

        // then
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(dto1, dto2);

        verify(protectingReportRepository, times(1)).findRandomReports(any(Pageable.class));
        verify(protectingReportDetailStrategy, times(2))
                .toDetailDto(any(ProtectingReport.class), anyList(), eq(false));

        // 이미지가 없어서 upload는 호출 X
        verifyNoInteractions(imageUploader);
    }

    @Test
    @DisplayName("요청 개수가 전체 개수보다 적으면 요청 개수만큼만 반환한다")
    void getRandomProtectingReportsWithS3_whenCountLessThanSize_thenReturnCountReports() {
        // given (보호글 3개, count=2)
        ProtectingReport r1 = mock(ProtectingReport.class);
        ProtectingReport r2 = mock(ProtectingReport.class);
        ProtectingReport r3 = mock(ProtectingReport.class);

        when(r1.getReportImages()).thenReturn(Collections.emptyList());
        when(r2.getReportImages()).thenReturn(Collections.emptyList());
        when(r3.getReportImages()).thenReturn(Collections.emptyList());

        when(protectingReportRepository.findRandomReports(any(Pageable.class)))
                .thenReturn(new ArrayList<>(List.of(r1, r2)));

        when(protectingReportDetailStrategy.toDetailDto(any(), anyList(), eq(false)))
                .thenReturn(mock(ProtectingReportDetailResponseDTO.class));

        // when
        List<ProtectingReportDetailResponseDTO> result =
                protectingReportRetrieveWithS3Service.getRandomProtectingReportsWithS3(2);

        // then
        assertThat(result).hasSize(2);

        verify(protectingReportRepository).findRandomReports(any(Pageable.class));
        verify(protectingReportDetailStrategy, times(2))
                .toDetailDto(any(ProtectingReport.class), anyList(), eq(false));
    }

    @Test
    @DisplayName("이미지 처리 중 예기치 못한 예외가 나도 전체 API는 실패하지 않는다")
    void getRandomProtectingReportsWithS3_whenUnexpectedException_thenContinue() {
        // given
        ProtectingReport report = mock(ProtectingReport.class);
        when(report.getId()).thenReturn(1L);

        // 존재하지 않는 로컬 포트로 URL 세팅 → RestTemplate 호출 시 예외 발생
        ReportImage img1 = mock(ReportImage.class);
        when(img1.getImageUrl()).thenReturn("http://localhost:65535/nonexistent");

        when(report.getReportImages()).thenReturn(List.of(img1));
        when(protectingReportRepository.findRandomReports(any(Pageable.class)))
                .thenReturn(new ArrayList<>(List.of(report)));

        ProtectingReportDetailResponseDTO dto = mock(ProtectingReportDetailResponseDTO.class);
        when(protectingReportDetailStrategy.toDetailDto(eq(report), anyList(), eq(false)))
                .thenReturn(dto);

        // when
        List<ProtectingReportDetailResponseDTO> result =
                protectingReportRetrieveWithS3Service.getRandomProtectingReportsWithS3(1);

        // then (예외 X, DTO는 정상적으로 하나 반환)
        assertThat(result)
                .hasSize(1)
                .containsExactly(dto);
    }
    @Test
    @DisplayName("이미지를 정상적으로 받으면 S3에 업로드 &  업로드된 URL로 DTO 생성")
    void getRandomProtectingReportsWithS3_successUploadFlow() {
        // given
        ProtectingReport report = mock(ProtectingReport.class);
        when(report.getId()).thenReturn(100L);

        ReportImage img1 = mock(ReportImage.class);
        ReportImage img2 = mock(ReportImage.class);
        when(img1.getImageUrl()).thenReturn("http://example.com/1.jpg");
        when(img2.getImageUrl()).thenReturn("http://example.com/2.jpg");
        when(report.getReportImages()).thenReturn(List.of(img1, img2));

        when(protectingReportRepository.findRandomReports(any(Pageable.class)))
                .thenReturn(new ArrayList<>(List.of(report)));

        // RestTemplate 가 바이트 배열 내려줌
        when(restTemplate.getForObject(eq("http://example.com/1.jpg"), eq(byte[].class)))
                .thenReturn(new byte[]{1, 2, 3});
        when(restTemplate.getForObject(eq("http://example.com/2.jpg"), eq(byte[].class)))
                .thenReturn(new byte[]{4, 5, 6});

        // S3 업로더는 순서대로 두 번 URL을 반환
        when(imageUploader.upload(any(byte[].class), anyString(), eq("image/jpeg")))
                .thenReturn(
                        "https://s3.findyou.store/1.jpg",
                        "https://s3.findyou.store/2.jpg"
                );

        ProtectingReportDetailResponseDTO dto = mock(ProtectingReportDetailResponseDTO.class);

        ArgumentCaptor<List<String>> s3UrlsCaptor = ArgumentCaptor.forClass(List.class);
        when(protectingReportDetailStrategy.toDetailDto(eq(report), s3UrlsCaptor.capture(), eq(false)))
                .thenReturn(dto);

        // when
        List<ProtectingReportDetailResponseDTO> result =
                protectingReportRetrieveWithS3Service.getRandomProtectingReportsWithS3(1);

        // then
        assertThat(result).hasSize(1).containsExactly(dto);

        List<String> capturedUrls = s3UrlsCaptor.getValue();
        assertThat(capturedUrls)
                .containsExactly(
                        "https://s3.findyou.store/1.jpg",
                        "https://s3.findyou.store/2.jpg"
                );

        verify(restTemplate, times(1))
                .getForObject("http://example.com/1.jpg", byte[].class);
        verify(restTemplate, times(1))
                .getForObject("http://example.com/2.jpg", byte[].class);
        verify(imageUploader, times(2))
                .upload(any(byte[].class), anyString(), eq("image/jpeg"));
    }

    @Test
    @DisplayName("S3 업로드나 이미지 처리에서 예외가 발생해도 DTO는 정상 반환된다")
    void getRandomProtectingReportsWithS3_whenUploadFails_thenContinuePerImage() {
        // given
        ProtectingReport report = mock(ProtectingReport.class);
        when(report.getId()).thenReturn(200L);

        //첫 번째는 S3 업로드 실패, 두 번째는 다운로드 단계 예외
        ReportImage img1 = mock(ReportImage.class);
        ReportImage img2 = mock(ReportImage.class);
        when(img1.getImageUrl()).thenReturn("http://example.com/1.jpg");
        when(img2.getImageUrl()).thenReturn("http://example.com/2.jpg");
        when(report.getReportImages()).thenReturn(List.of(img1, img2));

        when(protectingReportRepository.findRandomReports(any(Pageable.class)))
                .thenReturn(new ArrayList<>(List.of(report)));

        //1번 이미지는 S3 업로드 시 FileUploadingFailedException 발생
        when(restTemplate.getForObject("http://example.com/1.jpg", byte[].class))
                .thenReturn(new byte[]{1, 2, 3});
        when(imageUploader.upload(any(byte[].class), anyString(), eq("image/jpeg")))
                .thenThrow(new FileUploadingFailedException("업로드 실패"));

        //2번 이미지는 다운로드 단계에서 바로 RuntimeException
        when(restTemplate.getForObject("http://example.com/2.jpg", byte[].class))
                .thenThrow(new RuntimeException("다운로드 오류"));

        ProtectingReportDetailResponseDTO dto = mock(ProtectingReportDetailResponseDTO.class);
        ArgumentCaptor<List<String>> s3UrlsCaptor = ArgumentCaptor.forClass(List.class);
        when(protectingReportDetailStrategy.toDetailDto(eq(report), s3UrlsCaptor.capture(), eq(false)))
                .thenReturn(dto);

        // when
        List<ProtectingReportDetailResponseDTO> result =
                protectingReportRetrieveWithS3Service.getRandomProtectingReportsWithS3(1);

        // then
        //전체 API는 죽지 않고 DTO 하나는 정상 반환
        assertThat(result).hasSize(1).containsExactly(dto);

        //두 이미지 모두 실패했으므로 이미지는 빈값
        assertThat(s3UrlsCaptor.getValue()).isEmpty();

        //예외가 던져지지 않음
        verify(restTemplate, times(1))
                .getForObject("http://example.com/1.jpg", byte[].class);
        verify(restTemplate, times(1))
                .getForObject("http://example.com/2.jpg", byte[].class);
        verify(imageUploader, times(1))
                .upload(any(byte[].class), anyString(), eq("image/jpeg"));
    }

}
