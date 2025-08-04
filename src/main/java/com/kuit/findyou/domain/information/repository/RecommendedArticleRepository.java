package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.RecommendedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedArticleRepository extends JpaRepository<RecommendedArticle,Long> {
}
