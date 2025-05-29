package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.MissingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MissingReportRepository extends JpaRepository<MissingReport, Long> {
}
