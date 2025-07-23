package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.ProtectingReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProtectingReportRepository extends JpaRepository<ProtectingReport, Long> {

    @EntityGraph(attributePaths = {"reportImages"})
    Optional<ProtectingReport> findWithImagesById(Long id);

    List<ProtectingReport> findByNoticeNumberIn(Set<String> noticeNumbers);
}

