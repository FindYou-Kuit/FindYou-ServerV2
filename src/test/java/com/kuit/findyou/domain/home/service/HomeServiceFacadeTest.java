package com.kuit.findyou.domain.home.service;

import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
import com.kuit.findyou.domain.home.dto.response.ProtectingAnimalPreview;
import com.kuit.findyou.domain.home.dto.response.WitnessedOrMissingAnimalPreview;
import com.kuit.findyou.domain.report.model.ReportTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceFacadeTest {
    @InjectMocks
    private HomeServiceFacade homeServiceFacade;

    @Mock
    private HomeStatisticsService homeStatisticsService;

    @Mock
    private RetrieveHomeSectionService retrieveHomeSectionService;

    @DisplayName("요청의 위도 경도가 올바르면 홈화면 조회에 성공한다")
    @Test
    void should_Succeed_When_RequestedWithCorrectCoordinate(){
        // given
        final double lat = 33.0;
        final double lng = 127.0;
        final int limit = 10;

        GetHomeResponse.TotalStatistics cachedTotalStatistics = mock(GetHomeResponse.TotalStatistics.class);

        List<ProtectingAnimalPreview> protectingAnimals = LongStream.rangeClosed(1, 10)
                        .boxed()
                        .sorted(Comparator.reverseOrder())
                        .map(i -> new ProtectingAnimalPreview(i, "image" + i + ".png", "title" + i, ReportTag.PROTECTING.getValue(), LocalDate.of(2025, 1, 1), "place"))
                        .collect(toList());

        List<WitnessedOrMissingAnimalPreview> witnessedOrMissingAnimals = LongStream.rangeClosed(1, 10)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .map(i -> new WitnessedOrMissingAnimalPreview(i, "image" + i + ".png", "title" + i, ReportTag.PROTECTING.getValue(), LocalDate.of(2025, 1, 1), "place"))
                .collect(toList());

        when(homeStatisticsService.get()).thenReturn(cachedTotalStatistics);
        when(retrieveHomeSectionService.retrieveProtectingReportPreviews(eq(lat), eq(lng), eq(limit))).thenReturn(protectingAnimals);
        when(retrieveHomeSectionService.retrieveWitnessedOrMissingReportPreviews(eq(lat), eq(lng), eq(limit))).thenReturn(witnessedOrMissingAnimals);

        // when
        GetHomeResponse response = homeServiceFacade.getHome(lat, lng);

        // then
        assertThat(response.statistics()).isEqualTo(cachedTotalStatistics);
        assertThat(response.protectingAnimals().size()).isEqualTo(10);
        assertThat(response.protectingAnimals().get(0)).isEqualTo(protectingAnimals.get(0));
        assertThat(response.witnessedOrMissingAnimals().size()).isEqualTo(10);
        assertThat(response.witnessedOrMissingAnimals().get(0)).isEqualTo(witnessedOrMissingAnimals.get(0));
    }

    @DisplayName("요청에 위도 경도가 없어도 홈화면 조회에 성공한다")
    @Test
    void should_Succeed_When_RequestedWithoutCoordinate(){
        // given
        final int limit = 10;

        GetHomeResponse.TotalStatistics cachedTotalStatistics = mock(GetHomeResponse.TotalStatistics.class);

        List<ProtectingAnimalPreview> protectingAnimals = LongStream.rangeClosed(1, 10)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .map(i -> new ProtectingAnimalPreview(i, "image" + i + ".png", "title" + i, ReportTag.PROTECTING.getValue(), LocalDate.of(2025, 1, 1), "place"))
                .collect(toList());

        List<WitnessedOrMissingAnimalPreview> witnessedOrMissingAnimals = LongStream.rangeClosed(1, 10)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .map(i -> new WitnessedOrMissingAnimalPreview(i, "image" + i + ".png", "title" + i, ReportTag.PROTECTING.getValue(), LocalDate.of(2025, 1, 1), "place"))
                .collect(toList());

        when(homeStatisticsService.get()).thenReturn(cachedTotalStatistics);
        when(retrieveHomeSectionService.retrieveProtectingReportPreviews(eq(limit))).thenReturn(protectingAnimals);
        when(retrieveHomeSectionService.retrieveWitnessedOrMissingReportPreviews( eq(limit))).thenReturn(witnessedOrMissingAnimals);

        // when
        GetHomeResponse response = homeServiceFacade.getHome(null, null);

        // then
        assertThat(response.statistics()).isEqualTo(cachedTotalStatistics);
        assertThat(response.protectingAnimals().size()).isEqualTo(10);
        assertThat(response.protectingAnimals().get(0)).isEqualTo(protectingAnimals.get(0));
        assertThat(response.witnessedOrMissingAnimals().size()).isEqualTo(10);
        assertThat(response.witnessedOrMissingAnimals().get(0)).isEqualTo(witnessedOrMissingAnimals.get(0));
    }
}