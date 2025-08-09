package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.RecommendedNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedNewsRepository extends JpaRepository<RecommendedNews,Long> {
}
