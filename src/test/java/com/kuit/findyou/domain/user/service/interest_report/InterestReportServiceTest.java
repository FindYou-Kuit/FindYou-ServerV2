package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.user.dto.RetrieveInterestAnimalsResponse;
import com.kuit.findyou.domain.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.StatusResultMatchersExtensionsKt.isEqualTo;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class InterestReportServiceTest {
    @InjectMocks
    private InterestReportServiceImpl interestReportService;

    @Mock
    private InterestReportRepository interestReportRepository;

    @DisplayName("페이지 사이즈보다 많은 관심글이 존재하면 첫 조회 시에 마지막 페이지가 반환하지 않는다")
    @Test
    void should_NotReturnLastPage_When_NumberOfInterestAnimalsExceedsPageSize(){
        // given
        final Long userId = 1L;
        final Long lastId = Long.MAX_VALUE;
        final int size = 20;

        List<ReportProjection> projections = LongStream.iterate(size + 1, n -> n - 1)
                .limit(size + 1)
                .mapToObj(i -> new ReportProjectionImpl(i, "image" + i + ".png", "breed" + i, ReportTag.PROTECTING.getValue(), LocalDate.of(2025, 1, 1), "city"))
                .collect(Collectors.toList());

        when(interestReportRepository.findInterestReportsByCursor(userId, lastId, PageRequest.of(0, size + 1)))
                .thenReturn(projections);

        // when
        RetrieveInterestAnimalsResponse response = interestReportService.retrieveInterestAnimals(userId, lastId, size);

        // then
        assertThat(response.interestAnimals()).hasSize(size);
        assertThat(response.lastId()).isEqualTo(2L);
        assertThat(response.isLast()).isFalse();
    }

    @DisplayName("페이지 사이즈보다 적은 관심글이 존재하면 첫 조회 시에 마지막 페이지를 반환한다")
    @Test
    void should_ReturnLastPage_When_NumberOfInterestAnimalsIsSmallerThanPageSize(){
        // given
        final Long userId = 1L;
        final Long lastId = Long.MAX_VALUE;
        final int size = 20;

        List<ReportProjection> projections = LongStream.iterate(size - 1, n -> n - 1)
                .limit(size - 1)
                .mapToObj(i -> new ReportProjectionImpl(i, "image" + i + ".png", "breed" + i, ReportTag.PROTECTING.getValue(), LocalDate.of(2025, 1, 1), "city"))
                .collect(Collectors.toList());


        when(interestReportRepository.findInterestReportsByCursor(userId, lastId, PageRequest.of(0, size + 1)))
                .thenReturn(projections);

        // when
        RetrieveInterestAnimalsResponse response = interestReportService.retrieveInterestAnimals(userId, lastId, size);

        // then
        assertThat(response.interestAnimals()).hasSize(size - 1);
        assertThat(response.lastId()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }
}