package com.kuit.findyou.domain.recommendation.repository;

import com.kuit.findyou.domain.recommendation.model.RecommendedNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedNewsRepository extends JpaRepository<RecommendedNews,Long> {
}
