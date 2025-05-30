package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.ProtectingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProtectingReportRepository extends JpaRepository<ProtectingReport, Long> {
}

