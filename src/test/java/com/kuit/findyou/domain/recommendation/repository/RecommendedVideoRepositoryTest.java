package com.kuit.findyou.domain.recommendation.repository;

import com.kuit.findyou.domain.information.model.RecommendedVideo;
import com.kuit.findyou.domain.information.repository.RecommendedVideoRepository;
import com.kuit.findyou.global.config.TestDatabaseConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RecommendedVideoRepositoryTest {
    @Autowired
    private RecommendedVideoRepository videoRepository;

    @Test
    @DisplayName("추천 영상 저장 및 조회")
    void saveAndFindAll() {
        // given
        RecommendedVideo video = RecommendedVideo.builder()
                .title("강아지 영상")
                .uploader("찾아유TV")
                .url("https://youtube.com/v/1")
                .build();

        videoRepository.save(video);

        // when
        List<RecommendedVideo> result = videoRepository.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUploader()).isEqualTo("찾아유TV");
    }
}
