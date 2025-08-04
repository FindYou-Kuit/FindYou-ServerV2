package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.RecommendedVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedVideoRepository extends JpaRepository<RecommendedVideo,Long> {
}
