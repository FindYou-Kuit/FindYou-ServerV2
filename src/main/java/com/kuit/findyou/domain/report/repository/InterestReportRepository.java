package com.kuit.findyou.domain.report.repository;


import com.kuit.findyou.domain.report.model.InterestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterestReportRepository extends JpaRepository<InterestReport, Long> {


    @Query(value = "SELECT EXISTS (" +
            "SELECT 1 FROM interest_reports " +
            "WHERE report_id = :reportId " +
            "AND user_id = :userId " +
            "AND status = 'Y'" +
            ")", nativeQuery = true)
    boolean existsByReportIdAndUserId(@Param("reportId") Long reportId,
                                      @Param("userId") Long userId);


    @Query("""
                SELECT ir.report.id
                FROM InterestReport ir
                WHERE ir.user.id = :userId
                AND ir.report.id IN :reportIds
            """)
    List<Long> findInterestedReportIdsByUserIdAndReportIds(@Param("userId") Long userId, @Param("reportIds") List<Long> reportIds);


}
