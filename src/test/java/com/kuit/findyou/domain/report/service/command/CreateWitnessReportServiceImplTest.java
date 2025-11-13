package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.dto.request.CreateWitnessReportRequest;
import com.kuit.findyou.domain.report.model.WitnessReport;
import com.kuit.findyou.domain.report.repository.WitnessReportRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateWitnessReportServiceImplTest {
    @InjectMocks
    private CreateWitnessReportServiceImpl createWitnessReportService;

    @Mock
    private WitnessReportRepository witnessReportRepository;

    @Mock
    private ReportImageRepository reportImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KakaoCoordinateClient kakaoCoordinateClient;

    @Captor
    private ArgumentCaptor<WitnessReport> reportCaptor;

    @Captor
    private ArgumentCaptor<List<ReportImage>> imageListCaptor;

    private User testUser;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder().build();
    }

    // 테스트 데이터 생성을 위한 헬퍼 메서드
    private CreateWitnessReportRequest createValidRequest(List<String> imgUrls) {
        return new CreateWitnessReportRequest(
                imgUrls, "개", "포메라니안", "갈색",
                LocalDate.of(2025, 8, 30),"특이사항 없음", "서울시 광진구", "건대입구"
        );
    }

    @DisplayName("이미지와 함께 목격 신고글을 생성")
    @Test
    void createWitnessReport_withImages_Success() {
        // given
        CreateWitnessReportRequest request = createValidRequest(List.of("http://cdn.com/cat1.jpg", "http://cdn.com/cat2.jpg"));
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);

        var mockCoordinate = new KakaoCoordinateClient.Coordinate(new BigDecimal("37.544"), new BigDecimal("127.036"));
        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString())).thenReturn(mockCoordinate);

        // when
        createWitnessReportService.createWitnessReport(request, userId);

        // then
        verify(userRepository, times(1)).getReferenceById(userId);

        verify(witnessReportRepository, times(1)).save(reportCaptor.capture());
        WitnessReport capturedReport = reportCaptor.getValue();
        assertThat(capturedReport.getBreed()).isEqualTo(request.breed());
        assertThat(capturedReport.getUser()).isEqualTo(testUser);
        assertThat(capturedReport.getReporterName()).isEqualTo(testUser.getName());

        verify(reportImageRepository, times(1)).saveAll(imageListCaptor.capture());
        List<ReportImage> capturedImages = imageListCaptor.getValue();
        assertThat(capturedImages).hasSize(2);
        assertThat(capturedImages.get(0).getImageUrl()).isEqualTo("http://cdn.com/cat1.jpg");
        assertThat(capturedImages).allMatch(image -> image.getReport() == capturedReport);
    }

    @DisplayName("이미지 없이 목격 신고글 생성")
    @Test
    void createWitnessReport_withoutImages_Success() {
        // given
        CreateWitnessReportRequest request = createValidRequest(Collections.emptyList());
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);

        var mockCoordinate = new KakaoCoordinateClient.Coordinate(new BigDecimal("37.544"), new BigDecimal("127.036"));
        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString())).thenReturn(mockCoordinate);

        // when
        createWitnessReportService.createWitnessReport(request, userId);

        // then
        verify(witnessReportRepository, times(1)).save(any(WitnessReport.class));
        verifyNoInteractions(reportImageRepository);
    }

    @DisplayName("이미지 URL 리스트가 null일 때 성공")
    @Test
    void createWitnessReport_whenImageUrlListIsNull_Success() {
        // given
        // saveReportImages의 imageUrls == null 분기테스트
        CreateWitnessReportRequest request = createValidRequest(null);
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new KakaoCoordinateClient.Coordinate(BigDecimal.ONE, BigDecimal.ONE));

        // when
        createWitnessReportService.createWitnessReport(request, userId);

        // then
        verify(witnessReportRepository, times(1)).save(any(WitnessReport.class));
        verifyNoInteractions(reportImageRepository);
    }
}
