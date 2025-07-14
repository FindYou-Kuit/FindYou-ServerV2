package com.kuit.findyou.domain.report.repository;


import com.kuit.findyou.domain.report.model.InterestReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestReportRepository extends JpaRepository<InterestReport, Long> {

    boolean existsByReport_IdAndUser_Id(Long reportId, Long userId);

}
