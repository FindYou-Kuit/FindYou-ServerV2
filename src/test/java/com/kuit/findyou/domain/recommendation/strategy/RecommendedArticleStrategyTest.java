package com.kuit.findyou.domain.recommendation.strategy;

import com.kuit.findyou.domain.recommendation.model.RecommendedArticle;
import com.kuit.findyou.domain.recommendation.model.RecommendedVideo;
import com.kuit.findyou.domain.recommendation.repository.RecommendedArticleRepository;
import com.kuit.findyou.domain.recommendation.repository.RecommendedVideoRepository;
import com.kuit.findyou.domain.recommendation.service.strategy.RecommendedArticleStrategy;
import com.kuit.findyou.domain.recommendation.service.strategy.RecommendedVideoStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RecommendedArticleStrategyTest {

    @Mock
    private RecommendedArticleRepository articleRepository;

    @InjectMocks
    private RecommendedArticleStrategy strategy;

    @Test
    void getRecommendedContents_shouldReturnMappedDtoList() {
        // given
        var video1 = RecommendedArticle.builder()
                .title("강아지 기사")
                .uploader("찾아유")
                .url("news.com/1")
                .build();

        var video2 = RecommendedArticle.builder()
                .title("고양이 기사")
                .uploader("차자유")
                .url("news.com/2")
                .build();

        when(articleRepository.findAll()).thenReturn(List.of(video1, video2));

        // when
        var result = strategy.getRecommendedContents();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("title").containsExactly("강아지 기사", "고양이 기사");
        assertThat(result).extracting("uploader").containsExactly("찾아유", "차자유");
        assertThat(result).extracting("url").containsExactly("news.com/1", "news.com/2");
    }
}
