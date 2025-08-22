package com.kuit.findyou.domain.information.recommended.repository;

import com.kuit.findyou.domain.information.recommended.model.RecommendedNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedNewsRepository extends JpaRepository<RecommendedNews,Long> {
}
