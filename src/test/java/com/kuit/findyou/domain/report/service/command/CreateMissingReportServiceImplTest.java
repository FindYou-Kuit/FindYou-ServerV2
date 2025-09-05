package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;
import com.kuit.findyou.domain.report.dto.request.CreateWitnessReportRequest;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateMissingReportServiceImplTest {
    @InjectMocks
    private CreateMissingReportServiceImpl createMissingReportService;

    @Mock
    private MissingReportRepository missingReportRepository;

    @Mock
    private ReportImageRepository reportImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KakaoCoordinateClient kakaoCoordinateClient;

    @Captor
    private ArgumentCaptor<MissingReport> reportCaptor;

    @Captor
    private ArgumentCaptor<List<ReportImage>> imageListCaptor;

    private User testUser;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder().build();
    }

    // 테스트 데이터 생성을 위한 헬퍼 메서드
    private CreateMissingReportRequest createValidRequest(List<String> imgUrls) {
        return new CreateMissingReportRequest(
                imgUrls, "개", "포메라니안", "3살", "남자", "1234", "흰색",
                "2025.08.30", "특이사항 없음", "서울시 광진구", "건대입구"
        );
    }


    @DisplayName("이미지와 함께 실종 신고글을 생성")
    @Test
    void createMissingReport_withImages_Success() {
        // given
        CreateMissingReportRequest request = createValidRequest(List.of("http://cdn.com/url1.jpg", "http://cdn.com/url2.jpg"));
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);

        var mockCoordinate = new KakaoCoordinateClient.Coordinate(new BigDecimal("37.123"), new BigDecimal("127.123"));
        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString())).thenReturn(mockCoordinate);

        // when
        createMissingReportService.createMissingReport(request, userId);

        // then
        verify(userRepository, times(1)).getReferenceById(userId); //한번 호출 확인

        verify(missingReportRepository, times(1)).save(reportCaptor.capture());
        MissingReport capturedReport = reportCaptor.getValue();
        assertThat(capturedReport.getBreed()).isEqualTo(request.breed());
        assertThat(capturedReport.getUser()).isEqualTo(testUser);

        verify(reportImageRepository, times(1)).saveAll(imageListCaptor.capture());
        List<ReportImage> capturedImages = imageListCaptor.getValue();
        assertThat(capturedImages).hasSize(2);
        assertThat(capturedImages.get(0).getImageUrl()).isEqualTo("http://cdn.com/url1.jpg");
        //모든 이미지가 위에서 저장된 report와 잘 연결되었는지 확인
        assertThat(capturedImages).allMatch(image -> image.getReport() == capturedReport);
    }

    @DisplayName("이미지 없이 실종 신고글 생성")
    @Test
    void createMissingReport_withoutImages_Success() {
        // givrn
        CreateMissingReportRequest request = createValidRequest(Collections.emptyList()); // 빈 이미지 리스트
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);

        var mockCoordinate = new KakaoCoordinateClient.Coordinate(new BigDecimal("37.123"), new BigDecimal("127.123"));
        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString())).thenReturn(mockCoordinate);

        // when
        createMissingReportService.createMissingReport(request, userId);

        // then
        verify(missingReportRepository, times(1)).save(any(MissingReport.class));
        //이미지 없음 -> reportImageRepository의 어떤 메서드도 호출 X
        verifyNoInteractions(reportImageRepository);
    }


    @DisplayName("날짜 형식이 'yyyy.MM.dd'가 아니면 CustomException이 발생한다")
    @Test
    void createMissingReport_whenDateFormatIsInvalid_thenThrowsException() {
        // given
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        // when
        var request = new CreateMissingReportRequest(
                List.of("url"), "개", "말티즈", "3살", "남자", "1234", "흰색",
                "2025-08-30", // "yyyy.MM.dd" 형식이 아님
                "특이사항", "서울시", "건대입구"
        );

        // then
        assertThatThrownBy(() -> createMissingReportService.createMissingReport(request, userId))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getExceptionStatus()).isEqualTo(BAD_REQUEST);
                });

        //예외 발생 -> DB 저장 로직은 호출 X
        verifyNoInteractions(missingReportRepository, reportImageRepository);
    }


    @DisplayName("성별이 '여자'일 때 성공적으로 변환")
    @Test
    void createMissingReport_withFemaleSex_Success() {
        // given
        // mapSexStrict 메서드 분기 테스트
        CreateMissingReportRequest request = new CreateMissingReportRequest(
                List.of(), "개", "푸들", "3살", "여자", "1234", "흰색",
                "2025.08.30", "특이사항", "서울시", "건대입구"
        );
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new KakaoCoordinateClient.Coordinate(BigDecimal.ONE, BigDecimal.ONE));

        // when
        createMissingReportService.createMissingReport(request, userId);

        // then
        verify(missingReportRepository, times(1)).save(reportCaptor.capture());
        assertThat(reportCaptor.getValue().getSex()).isEqualTo(com.kuit.findyou.domain.report.model.Sex.F);
    }

    @DisplayName("성별이 유효하지 않은 값이면 CustomException 발생")
    @Test
    void createMissingReport_whenSexIsInvalid_thenThrowsException() {
        // given
        // mapSexStrict 메서드 예외 발생 분기 테스트
        CreateMissingReportRequest request = new CreateMissingReportRequest(
                List.of(), "개", "푸들", "3살", "알수없음", "1234", "흰색",
                "2025.08.30", "특이사항", "서울시", "건대입구"
        );
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new KakaoCoordinateClient.Coordinate(BigDecimal.ONE, BigDecimal.ONE));

        // when & then
        assertThatThrownBy(() -> createMissingReportService.createMissingReport(request, userId))
                .isInstanceOf(CustomException.class);
    }

    @DisplayName("이미지 URL 리스트가 null일 때 성공")
    @Test
    void createMissingReport_whenImageUrlListIsNull_Success() {
        // given
        //imageUrls == null
        CreateMissingReportRequest request = createValidRequest(null);
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new KakaoCoordinateClient.Coordinate(BigDecimal.ONE, BigDecimal.ONE));

        // when
        createMissingReportService.createMissingReport(request, userId);

        // then
        verify(missingReportRepository, times(1)).save(any(MissingReport.class));
        verifyNoInteractions(reportImageRepository);
    }

}
