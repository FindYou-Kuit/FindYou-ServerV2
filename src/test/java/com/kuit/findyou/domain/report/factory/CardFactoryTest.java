package com.kuit.findyou.domain.report.factory;

import com.kuit.findyou.domain.report.dto.response.Card;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
class CardFactoryTest {

    @Mock
    private InterestReportRepository interestReportRepository;

    @InjectMocks
    CardFactory cardFactory;

    @Test
    @DisplayName("ReportProjection을 기반으로 CardResponseDTO 생성 테스트")
    void createCardResponseTest() {
        // given
        Long userId = 1L;
        Long lastId = 999L;

        boolean isLast = true;

        ReportProjection projection1 = mockProjection(101L, "http://image1.jpg", "제목1", "MISSING", LocalDate.of(2024, 7, 20), "서울시 강남구");
        ReportProjection projection2 = mockProjection(102L, "http://image2.jpg", "제목2", "PROTECTING", LocalDate.of(2024, 7, 21), "서울시 마포구");

        List<ReportProjection> projections = List.of(projection1, projection2);

        // 관심글은 102번만 포함
        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(eq(userId), anyList()))
                .thenReturn(List.of(102L));

        // when
        CardResponseDTO result = cardFactory.createCardResponse(projections, userId, lastId, isLast);

        // then
        assertThat(result.cards()).hasSize(2);

        Card card1 = result.cards().get(0);
        assertThat(card1.reportId()).isEqualTo(101L);
        assertThat(card1.interest()).isFalse();

        Card card2 = result.cards().get(1);
        assertThat(card2.reportId()).isEqualTo(102L);
        assertThat(card2.interest()).isTrue();

        assertThat(result.lastId()).isEqualTo(lastId);
        assertThat(result.isLast()).isEqualTo(isLast);
    }



    private ReportProjection mockProjection(Long id, String imageUrl, String title, String tag, LocalDate date, String address) {
        ReportProjection mock = mock(ReportProjection.class);
        when(mock.getReportId()).thenReturn(id);
        when(mock.getThumbnailImageUrl()).thenReturn(imageUrl);
        when(mock.getTitle()).thenReturn(title);
        when(mock.getTag()).thenReturn(tag);
        when(mock.getDate()).thenReturn(date);
        when(mock.getAddress()).thenReturn(address);
        return mock;
    }

}