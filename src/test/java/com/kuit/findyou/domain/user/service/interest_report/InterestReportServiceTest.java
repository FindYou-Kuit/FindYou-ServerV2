package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.DUPLICATE_INTEREST_REPORT;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.REPORT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class InterestReportServiceTest {

    @InjectMocks
    private InterestReportServiceImpl interestReportService;

    @Mock
    private InterestReportRepository interestReportRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardFactory cardFactory;

    @Captor
    private ArgumentCaptor<List<ReportProjection>> projectionsCaptor;
    @Captor
    private ArgumentCaptor<Set<Long>> idsCaptor;
    @Captor
    private ArgumentCaptor<Long> nextLastIdCaptor;
    @Captor
    private ArgumentCaptor<Boolean> isLastCaptor;
    @Captor
    private ArgumentCaptor<Long> userIdCaptor;
    @Captor
    private ArgumentCaptor<Long> reportIdCaptor;

    @DisplayName("페이지 사이즈보다 많은 관심글이 존재하면 사이즈에 맞춰서 보여준다")
    @Test
    void should_TrimInterestAnimals_When_NumberOfThemExceedsPageSize() {
        // given
        final Long userId = 1L;
        final Long lastId = Long.MAX_VALUE;
        final int size = 20;

        List<ReportProjection> projections = getReportProjections(size + 1);
        when(interestReportRepository.findInterestReportsByCursor(
                eq(userId), eq(lastId), argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == size + 1)
        )).thenReturn(projections);

        CardResponseDTO expectedResponse = new CardResponseDTO(List.of(), -1L, false);
        when(cardFactory.createCardResponse(anyList(), anySet(), anyLong(), anyBoolean()))
                .thenReturn(expectedResponse);

        // when
        CardResponseDTO actualResponse = interestReportService.retrieveInterestAnimals(userId, lastId, size);

        // then
        verify(interestReportRepository).findInterestReportsByCursor(eq(userId), eq(lastId), any(PageRequest.class));

        verify(cardFactory).createCardResponse(
                projectionsCaptor.capture(),
                idsCaptor.capture(),
                nextLastIdCaptor.capture(),
                isLastCaptor.capture()
        );
        assertThat(projectionsCaptor.getValue()).hasSize(size);
        assertThat(idsCaptor.getValue()).containsAll(
                projectionsCaptor.getValue().stream().map(ReportProjection::getReportId).toList()
        );
        assertThat(nextLastIdCaptor.getValue()).isEqualTo(projectionsCaptor.getValue().get(size - 1).getReportId());
        assertThat(isLastCaptor.getValue()).isFalse();

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @DisplayName("페이지 사이즈보다 적은 관심글이 존재하면 그대로 반환한다")
    @Test
    void should_ReturnAllOfInterestAnimals_When_NumberOfThemIsSmallerThanPageSize() {
        // given
        final Long userId = 1L;
        final Long lastId = Long.MAX_VALUE;
        final int size = 20;

        List<ReportProjection> projections = getReportProjections(size - 1);
        when(interestReportRepository.findInterestReportsByCursor(
                eq(userId), eq(lastId), argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == size + 1)
        )).thenReturn(projections);

        CardResponseDTO expectedResponse = new CardResponseDTO(List.of(), -1L, true);
        when(cardFactory.createCardResponse(anyList(), anySet(), anyLong(), anyBoolean()))
                .thenReturn(expectedResponse);

        // when
        CardResponseDTO actualResponse = interestReportService.retrieveInterestAnimals(userId, lastId, size);

        // then
        verify(cardFactory).createCardResponse(
                projectionsCaptor.capture(),
                idsCaptor.capture(),
                nextLastIdCaptor.capture(),
                isLastCaptor.capture()
        );

        assertThat(projectionsCaptor.getValue()).hasSize(projections.size());
        assertThat(idsCaptor.getValue()).containsAll(
                projectionsCaptor.getValue().stream().map(ReportProjection::getReportId).toList()
        );
        assertThat(nextLastIdCaptor.getValue()).isEqualTo(projectionsCaptor.getValue().get(projectionsCaptor.getValue().size() - 1).getReportId());
        assertThat(isLastCaptor.getValue()).isTrue();

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    private List<ReportProjection> getReportProjections(int size) {
        return LongStream.iterate(size, n -> n - 1)
                .limit(size)
                .mapToObj(i -> new ReportProjectionImpl(
                        i,
                        "image" + i + ".png",
                        "breed" + i,
                        ReportTag.PROTECTING.getValue(),
                        LocalDate.of(2025, 1, 1),
                        "city"
                ))
                .collect(Collectors.toList());
    }

    @DisplayName("새로운 관심동물을 등록하면 성공한다")
    @Test
    void shouldSucceed_WhenNewInterestAnimalIsAdded(){
        // given
        final long userId = 1L;
        final long reportId = 2L;

        User mockUser = mock(User.class);
        Report mockReport = mock(Report.class);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(reportRepository.findById(anyLong())).thenReturn(Optional.of(mockReport));
        when(interestReportRepository.existsByReportIdAndUserId(anyLong(), anyLong())).thenReturn(false);

        // when
        interestReportService.addInterestAnimal(userId, reportId);

        // then
        verify(userRepository, times(1)).findById(userIdCaptor.capture());
        Long actualUserId = userIdCaptor.getValue();
        assertThat(actualUserId).isEqualTo(userId);

        verify(reportRepository, times(1)).findById(reportIdCaptor.capture());
        Long actualReportId = reportIdCaptor.getValue();
        assertThat(actualReportId).isEqualTo(reportId);

        verify(interestReportRepository, times(1)).existsByReportIdAndUserId(reportIdCaptor.capture(), userIdCaptor.capture());
        actualReportId = reportIdCaptor.getValue();
        actualUserId = userIdCaptor.getValue();
        assertThat(actualUserId).isEqualTo(userId);
        assertThat(actualReportId).isEqualTo(reportId);
    }

    @DisplayName("이미 등록했던 관심동물을 다시 등록하면 예외가 발생한다")
    @Test
    void shouldThrowException_WhenInterestReportRequestIsDuplicate(){
        // given
        final long userId = 1L;
        final long reportId = 2L;

        User mockUser = mock(User.class);
        Report mockReport = mock(Report.class);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(reportRepository.findById(anyLong())).thenReturn(Optional.of(mockReport));
        when(interestReportRepository.existsByReportIdAndUserId(anyLong(), anyLong())).thenReturn(true);

        // when && then
        assertThatThrownBy(() -> interestReportService.addInterestAnimal(userId, reportId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(DUPLICATE_INTEREST_REPORT.getMessage());
    }

    @DisplayName("존재하지 않는 동물을 관심동물로 등록하면 예외가 발생한다")
    @Test
    void shouldThrowException_WhenAnimalInAddInterestAnimalRequestDoesNotExist(){
        // given
        final long userId = 1L;
        final long reportId = 2L;

        User mockUser = mock(User.class);
        Report mockReport = mock(Report.class);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(reportRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when && then
        assertThatThrownBy(() -> interestReportService.addInterestAnimal(userId, reportId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(REPORT_NOT_FOUND.getMessage());
    }
}
