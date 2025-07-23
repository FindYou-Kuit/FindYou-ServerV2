package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.ViewedReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ViewedReportRepository extends JpaRepository<ViewedReport, Long> {

    @Modifying
    @Query(value = "DELETE FROM viewed_reports WHERE user_id = :userId AND report_id = :reportId", nativeQuery = true)
    void deleteByUserIdAndReportId(@Param("userId") Long userId, @Param("reportId") Long reportId);

    Slice<ViewedReport> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long lastViewedReportId, Pageable pageable);
}
