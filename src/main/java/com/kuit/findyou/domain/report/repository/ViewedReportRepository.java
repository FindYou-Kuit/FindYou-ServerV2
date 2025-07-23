package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.ViewedReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewedReportRepository extends JpaRepository<ViewedReport, Long> {
}
