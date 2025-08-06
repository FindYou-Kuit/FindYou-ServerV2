package com.kuit.findyou.domain.recommendation.repository;

import com.kuit.findyou.domain.recommendation.model.RecommendedVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedVideoRepository extends JpaRepository<RecommendedVideo,Long> {
}
