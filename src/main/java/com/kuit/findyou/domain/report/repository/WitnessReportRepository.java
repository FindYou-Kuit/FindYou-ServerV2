package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.model.WitnessReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WitnessReportRepository extends JpaRepository<WitnessReport, Long> {

    @EntityGraph(attributePaths = {"reportImages"})
    Optional<WitnessReport> findWithImagesById(Long id);
}

