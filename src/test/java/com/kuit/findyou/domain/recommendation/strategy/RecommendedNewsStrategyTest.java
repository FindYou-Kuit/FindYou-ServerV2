package com.kuit.findyou.domain.recommendation.strategy;

import com.kuit.findyou.domain.information.model.RecommendedNews;
import com.kuit.findyou.domain.information.repository.RecommendedNewsRepository;
import com.kuit.findyou.domain.information.service.strategy.RecommendedNewsStrategy;
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
public class RecommendedNewsStrategyTest {

    @Mock
    private RecommendedNewsRepository newsRepository;

    @InjectMocks
    private RecommendedNewsStrategy strategy;

    @Test
    void getRecommendedContents_shouldReturnMappedDtoList() {
        // given
        var video1 = RecommendedNews.builder()
                .title("강아지 기사")
                .uploader("찾아유")
                .url("news.com/1")
                .build();

        var video2 = RecommendedNews.builder()
                .title("고양이 기사")
                .uploader("차자유")
                .url("news.com/2")
                .build();

        when(newsRepository.findAll()).thenReturn(List.of(video1, video2));

        // when
        var result = strategy.getRecommendedContents();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("title").containsExactly("강아지 기사", "고양이 기사");
        assertThat(result).extracting("uploader").containsExactly("찾아유", "차자유");
        assertThat(result).extracting("url").containsExactly("news.com/1", "news.com/2");
    }
}
