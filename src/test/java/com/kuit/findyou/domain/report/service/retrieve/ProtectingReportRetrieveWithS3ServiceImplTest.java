package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.ProtectingReportDetailStrategy;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
class ProtectingReportRetrieveWithS3ServiceImplTest {

    @Mock
    private ProtectingReportRepository protectingReportRepository;

    @Mock
    private ImageUploader imageUploader;

    @Mock
    private ProtectingReportDetailStrategy protectingReportDetailStrategy;

    @InjectMocks
    private ProtectingReportRetrieveWithS3ServiceImpl protectingReportRetrieveWithS3Service;

    @Test
    @DisplayName("보호글이 하나도 없으면 PROTECTING_REPORT_NOT_FOUND 예외")
    void getRandomProtectingReportsWithS3_whenNoReports_thenThrow() {
        // given
        when(protectingReportRepository.findAll()).thenReturn(List.of());

        // when & then
        assertThrows(CustomException.class,
                () -> protectingReportRetrieveWithS3Service.getRandomProtectingReportsWithS3(3)
        );

        verify(protectingReportRepository, times(1)).findAll();
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

        when(protectingReportRepository.findAll())
                .thenReturn(List.of(report1, report2));

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

        verify(protectingReportRepository, times(1)).findAll();
        verify(protectingReportDetailStrategy, times(2))
                .toDetailDto(any(ProtectingReport.class), anyList(), eq(false));

        // 이미지가 없어서 upload는 호출 X
        verifyNoInteractions(imageUploader);
    }
}
