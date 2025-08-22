package com.kuit.findyou.domain.information.recommended.repository;

import com.kuit.findyou.domain.information.recommended.model.RecommendedVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedVideoRepository extends JpaRepository<RecommendedVideo,Long> {
}
