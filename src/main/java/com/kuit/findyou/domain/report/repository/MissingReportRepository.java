package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MissingReportRepository extends JpaRepository<MissingReport, Long> {

    @EntityGraph(attributePaths = {"reportImages"})
    Optional<MissingReport> findWithImagesById(Long id);
}
