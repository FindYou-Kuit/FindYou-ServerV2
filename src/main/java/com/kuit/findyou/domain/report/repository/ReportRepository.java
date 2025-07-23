package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByTag(ReportTag tag);

    @Query("""
                SELECT
                    r.id AS reportId,
                    (
                        SELECT ri.imageUrl
                        FROM ReportImage ri
                        WHERE ri.report.id = r.id
                        ORDER BY ri.id ASC
                        LIMIT 1
                    ) AS thumbnailImageUrl,
                    r.breed AS title,
                    r.tag AS tag,
                    r.date AS date,
                    r.address AS address
                FROM Report r
                WHERE r.id < :lastId
                  AND (:tags IS NULL OR r.tag IN :tags)
                  AND (:startDate IS NULL OR r.date >= :startDate)
                  AND (:endDate IS NULL OR r.date <= :endDate)
                  AND (:species IS NULL OR r.species LIKE CONCAT('%', :species, '%'))
                  AND (:breeds IS NULL OR r.breed IN :breeds)
                  AND (:address IS NULL OR r.address LIKE CONCAT('%', :address, '%'))
                ORDER BY r.id DESC
            """)
    Slice<ReportProjection> findReportsWithFilters(
            @Param("tags") List<ReportTag> tags,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("species") String species,
            @Param("breeds") List<String> breeds,
            @Param("address") String address,
            @Param("lastId") Long lastId,
            Pageable pageable
    );


}

