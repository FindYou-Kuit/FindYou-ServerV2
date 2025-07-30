package com.kuit.findyou.domain.recommendation.repository;

import com.kuit.findyou.domain.recommendation.model.RecommendedArticle;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
public class RecommendedArticleRepositoryTest {
    @Autowired
    private RecommendedArticleRepository articleRepository;

    @Test
    @DisplayName("추천 기사 저장 및 조회")
    void saveAndFindAll() {
        // given
        RecommendedArticle article = RecommendedArticle.builder()
                .title("강아지 영상")
                .source("찾아유 일보")
                .url("https://news.com/v/1")
                .build();

        articleRepository.save(article);

        // when
        List<RecommendedArticle> result = articleRepository.findAll();

        // then
        assertThat(result).hasSize(1);
        AssertionsForClassTypes.assertThat(result.get(0).getSource()).isEqualTo("찾아유 일보");
    }
}
