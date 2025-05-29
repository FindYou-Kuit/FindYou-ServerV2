package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.WitnessReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WitnessReportRepository extends JpaRepository<WitnessReport, Long> {
}

