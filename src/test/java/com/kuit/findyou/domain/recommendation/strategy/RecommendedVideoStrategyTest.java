package com.kuit.findyou.domain.recommendation.strategy;

import com.kuit.findyou.domain.recommendation.model.RecommendedVideo;
import com.kuit.findyou.domain.recommendation.repository.RecommendedVideoRepository;
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
public class RecommendedVideoStrategyTest {

    @Mock
    private RecommendedVideoRepository videoRepository;

    @InjectMocks
    private RecommendedVideoStrategy strategy;

    @Test
    void getRecommendedContents_shouldReturnMappedDtoList() {
        // given
        var video1 = RecommendedVideo.builder()
                .title("강아지 영상")
                .uploader("찾아유 채널")
                .url("youtube.com/1")
                .build();

        var video2 = RecommendedVideo.builder()
                .title("고양이 영상")
                .uploader("차자유 채널")
                .url("youtube.com/2")
                .build();

        when(videoRepository.findAll()).thenReturn(List.of(video1, video2));

        // when
        var result = strategy.getRecommendedContents();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("title").containsExactly("강아지 영상", "고양이 영상");
        assertThat(result).extracting("uploader").containsExactly("찾아유 채널", "차자유 채널");
        assertThat(result).extracting("url").containsExactly("youtube.com/1", "youtube.com/2");
    }
}
