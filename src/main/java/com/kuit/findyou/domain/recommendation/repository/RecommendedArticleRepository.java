package com.kuit.findyou.domain.recommendation.repository;

import com.kuit.findyou.domain.recommendation.model.RecommendedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedArticleRepository extends JpaRepository<RecommendedArticle,Long> {
}
