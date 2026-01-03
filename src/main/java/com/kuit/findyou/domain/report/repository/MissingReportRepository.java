package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.MissingReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface MissingReportRepository extends JpaRepository<MissingReport, Long> {

    @EntityGraph(attributePaths = {"reportImages"})
    Optional<MissingReport> findWithImagesById(Long id);

    List<MissingReport> findByDate(LocalDate date);
}
