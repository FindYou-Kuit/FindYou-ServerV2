package com.kuit.findyou.domain.home.service;

import com.kuit.findyou.domain.home.dto.response.ProtectingAnimalPreview;
import com.kuit.findyou.domain.home.dto.response.WitnessedOrMissingAnimalPreview;
import com.kuit.findyou.domain.home.service.card.RetrieveHomeSectionServiceImpl;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetrieveHomeSectionServiceTest {
    @InjectMocks
    private RetrieveHomeSectionServiceImpl service;

    @Mock
    private ReportRepository reportRepository;

    @DisplayName("좌표 기반 보호중 미리보기: PROTECTING 태그와 PageRequest(0,size)로 조회하고 of로 매핑한다")
    @Test
    void should_RetrieveProtectingSucceedReportPreviews_When_RequestWithCoords() {
        // given
        Double lat = 37.5, lng = 127.0;
        int size = 10;

        ReportProjection p1 = mock(ReportProjection.class);
        ReportProjection p2 = mock(ReportProjection.class);
        when(reportRepository.findNearestReports(eq(lat), eq(lng),
                eq(List.of(ReportTag.PROTECTING)), eq(PageRequest.of(0, size))))
                .thenReturn(List.of(p1, p2));

        ProtectingAnimalPreview v1 = mock(ProtectingAnimalPreview.class);
        ProtectingAnimalPreview v2 = mock(ProtectingAnimalPreview.class);

        try (MockedStatic<ProtectingAnimalPreview> mocked = Mockito.mockStatic(ProtectingAnimalPreview.class)) {
            mocked.when(() -> ProtectingAnimalPreview.of(p1)).thenReturn(v1);
            mocked.when(() -> ProtectingAnimalPreview.of(p2)).thenReturn(v2);

            // when
            List<ProtectingAnimalPreview> result = service.retrieveProtectingReportPreviews(lat, lng, size);

            // then
            assertThat(result).containsExactly(v1, v2);
            mocked.verify(() -> ProtectingAnimalPreview.of(p1));
            mocked.verify(() -> ProtectingAnimalPreview.of(p2));
        }
    }

    @DisplayName("좌표 기반 목격/실종 미리보기: WITNESS, MISSING 태그로 조회한다")
    @Test
    void shouldRetrieveWitnessOrMissingReportPreviews_When_RequestwithCoords() {
        // given
        Double lat = 37.5, lng = 127.0;
        int size = 10;

        ReportProjection p = mock(ReportProjection.class);
        when(reportRepository.findNearestReports(eq(lat), eq(lng),
                eq(List.of(ReportTag.WITNESS, ReportTag.MISSING)), eq(PageRequest.of(0, size))))
                .thenReturn(List.of(p));

        WitnessedOrMissingAnimalPreview view = mock(WitnessedOrMissingAnimalPreview.class);
        try (MockedStatic<WitnessedOrMissingAnimalPreview> mocked = Mockito.mockStatic(WitnessedOrMissingAnimalPreview.class)) {
            mocked.when(() -> WitnessedOrMissingAnimalPreview.of(p)).thenReturn(view);

            // when
            List<WitnessedOrMissingAnimalPreview> result =
                    service.retrieveWitnessedOrMissingReportPreviews(lat, lng, size);

            // then
            assertThat(result).containsExactly(view);
        }
    }

    @DisplayName("좌표 없이 보호중: findReportsWithFilters로 페이지 0,size 조회하고 매핑한다")
    @Test
    void shouldRetrieveProtectingReportPreviews_When_RequestWithoutCoords() {
        // given
        int size = 10;
        Page<ReportProjection> page = new PageImpl<>(List.of(mock(ReportProjection.class)));

        when(reportRepository.findReportsWithFilters(
                eq(List.of(ReportTag.PROTECTING)), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(Long.MAX_VALUE), eq(PageRequest.of(0, size))))
                .thenReturn(page);

        ProtectingAnimalPreview view = mock(ProtectingAnimalPreview.class);
        try (MockedStatic<ProtectingAnimalPreview> mocked = Mockito.mockStatic(ProtectingAnimalPreview.class)) {
            mocked.when(() -> ProtectingAnimalPreview.of(any())).thenReturn(view);

            // when
            List<ProtectingAnimalPreview> result = service.retrieveProtectingReportPreviews(size);

            // then
            assertThat(result).containsExactly(view);
        }
    }

    @DisplayName("좌표 없이 목격/실종: findReportsWithFilters로 조회")
    @Test
    void shouldRetrieveWitnessOrMissingReportPreviews_WhenRequestWithoutCoords() {
        // given
        int size = 10;
        Page<ReportProjection> page = new PageImpl<>(List.of());

        when(reportRepository.findReportsWithFilters(
                eq(List.of(ReportTag.WITNESS, ReportTag.MISSING)), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(Long.MAX_VALUE), eq(PageRequest.of(0, size))))
                .thenReturn(page);

        // when
        List<WitnessedOrMissingAnimalPreview> result = service.retrieveWitnessedOrMissingReportPreviews(size);

        // then
        assertThat(result).isEmpty();
    }
}