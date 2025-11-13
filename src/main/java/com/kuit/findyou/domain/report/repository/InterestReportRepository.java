package com.kuit.findyou.domain.report.repository;


import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.InterestReport;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.user.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterestReportRepository extends JpaRepository<InterestReport, Long> {


    boolean existsByReportIdAndUserId(@Param("reportId") Long reportId,
                                      @Param("userId") Long userId);


    @Query("""
                SELECT ir.report.id
                FROM InterestReport ir
                WHERE ir.user.id = :userId
                AND ir.report.id IN :reportIds
            """)
    List<Long> findInterestedReportIdsByUserIdAndReportIds(@Param("userId") Long userId, @Param("reportIds") List<Long> reportIds);

    @Query("""
        SELECT ir.report.id AS reportId,
            (SELECT ri.imageUrl FROM ReportImage ri WHERE ri.report.id = ir.report.id ORDER BY ri.id ASC LIMIT 1) AS thumbnailImageUrl,
            ir.report.breed AS breed,
            ir.report.tag AS tag,
            ir.report.date AS date,
            ir.report.address AS address
        FROM InterestReport ir JOIN ir.report
        WHERE ir.id < :lastId AND ir.user.id = :userId
        ORDER BY ir.id DESC
    """)
    List<ReportProjection> findInterestReportsByCursor(@Param("userId") Long userId, @Param("lastId") Long lastId, Pageable pageable);

    void deleteByUserAndReport(User user, Report report);
}
