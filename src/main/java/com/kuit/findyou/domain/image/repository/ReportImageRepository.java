package com.kuit.findyou.domain.image.repository;

import com.kuit.findyou.domain.image.model.ReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface ReportImageRepository extends JpaRepository<ReportImage, Long> {

    @Query("select ri.imageUrl from ReportImage ri join ri.report r where r.tag = 'MISSING'")
    Set<String> findAllImageUrlsForMissing();
}
