package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.MISMATCH_REPORT_USER;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.REPORT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteReportServiceImplTest {
    @InjectMocks
    private DeleteReportServiceImpl deleteReportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ImageUploader imageUploader;

    private User user;
    private Report reportWithoutImages;
    private Report reportWithImages;
    private Long userId = 1L;
    private Long reportId = 10L;

    @BeforeEach
    void setUp() {
        user = User.builder().id(userId).build();

        //이미지 없는 신고글
        reportWithoutImages = MissingReport.builder().user(user).build();

        //이미지 있는 신고글
        reportWithImages = MissingReport.builder().user(user).build();
        ReportImage.createReportImage("https://cdn.findyou.store/image.jpg", reportWithImages);
    }
    @Test
    @DisplayName("이미지가 없는 신고글을 성공적으로 삭제")
    void deleteReport_WithoutImages_Success() {
        // given
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportWithoutImages));

        // when
        deleteReportService.deleteReport(reportId, userId);

        // then
        verify(reportRepository, times(1)).findById(reportId);
        verify(imageUploader, never()).delete(anyString());
        verify(reportRepository, times(1)).delete(reportWithoutImages);
    }

    @Test
    @DisplayName("이미지가 있는 신고글을 성공적으로 삭제")
    void deleteReport_WithImages_Success() {
        // given
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportWithImages));

        // when
        deleteReportService.deleteReport(reportId, userId);

        // then
        verify(reportRepository, times(1)).findById(reportId);
        // imageUploader의 delete가 1번 호출되었는지 검증
        verify(imageUploader, times(1)).delete(anyString());
        verify(reportRepository, times(1)).delete(reportWithImages);
    }

    @Test
    @DisplayName("존재하지 않는 신고글을 삭제하려 하면 예외 발생")
    void deleteReport_Fail_ReportNotFound() {
        // given

        when(reportRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deleteReportService.deleteReport(reportId, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("exceptionStatus", REPORT_NOT_FOUND);

        //delete 메서드는 호출되지 않아야 함
        verify(reportRepository, never()).delete(any());
        verify(imageUploader, never()).delete(anyString());
    }

    @Test
    @DisplayName("다른 사람의 신고글을 삭제하려고 하면 예외가 발생")
    void deleteReport_Fail_UserMismatch() {
        // given
        Long otherUserId = 2L; //글 작성자와 다른 유저

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportWithoutImages));

        // when & then
        //다른 유저 ID로 deleteReport 메서드 실행
        assertThatThrownBy(() -> deleteReportService.deleteReport(reportId, otherUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("exceptionStatus", MISMATCH_REPORT_USER);

        //delete 메서드는 호출되지 않아야 함
        verify(reportRepository, never()).delete(any());
        verify(imageUploader, never()).delete(anyString());
    }
}
