package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.ProtectingReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProtectingReportRepository extends JpaRepository<ProtectingReport, Long> {

    @EntityGraph(attributePaths = {"reportImages"})
    Optional<ProtectingReport> findWithImagesById(Long id);
}

