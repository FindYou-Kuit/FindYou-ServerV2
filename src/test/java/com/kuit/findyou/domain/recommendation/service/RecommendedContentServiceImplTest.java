package com.kuit.findyou.domain.recommendation.service;

import com.kuit.findyou.domain.information.dto.ContentType;
import com.kuit.findyou.domain.information.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.service.recommended.RecommendedContentServiceImpl;
import com.kuit.findyou.domain.information.service.recommended.strategy.RecommendedContentStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class RecommendedContentServiceImplTest {

    @Test
    @DisplayName("VIDEO 타입에 대응하는 전략을 사용해 추천 콘텐츠를 반환한다")
    void getVideoContents_success() {
        // given
        RecommendedContentStrategy mockStrategy = mock(RecommendedContentStrategy.class);
        List<RecommendedContentResponse> mockResponse = List.of(
                new RecommendedContentResponse("video1", "uploader1", "url1"),
                new RecommendedContentResponse("video2", "uploader2", "url2")
        );
        when(mockStrategy.getRecommendedContents()).thenReturn(mockResponse);

        RecommendedContentServiceImpl service = new RecommendedContentServiceImpl(
                Map.of(ContentType.VIDEO, mockStrategy)
        );

        // when
        List<RecommendedContentResponse> result = service.getContents(ContentType.VIDEO);

        // then
        assertThat(result).hasSize(2);
        verify(mockStrategy, times(1)).getRecommendedContents();
    }
    @Test
    @DisplayName("NEWS 타입에 대응하는 전략을 사용해 추천 콘텐츠를 반환한다")
    void getNEWSContents_success() {
        // given
        RecommendedContentStrategy mockStrategy = mock(RecommendedContentStrategy.class);
        List<RecommendedContentResponse> mockResponse = List.of(
                new RecommendedContentResponse("NEWS1", "uploader1", "url1"),
                new RecommendedContentResponse("NEWS2", "uploader2", "url2")
        );
        when(mockStrategy.getRecommendedContents()).thenReturn(mockResponse);

        RecommendedContentServiceImpl service = new RecommendedContentServiceImpl(
                Map.of(ContentType.NEWS, mockStrategy)
        );

        // when
        List<RecommendedContentResponse> result = service.getContents(ContentType.NEWS);

        // then
        assertThat(result).hasSize(2);
        verify(mockStrategy, times(1)).getRecommendedContents();
    }

    @Test
    @DisplayName("전략이 매핑되지 않은 타입일 경우 예외를 던진다")
    void getContents_strategyNotFound() {
        // given
        RecommendedContentServiceImpl service = new RecommendedContentServiceImpl(Map.of()); // 빈 맵

        // when & then
        assertThatThrownBy(() -> service.getContents(ContentType.VIDEO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 콘텐츠 타입의 처리 전략이 존재하지 않습니다.");
    }
}