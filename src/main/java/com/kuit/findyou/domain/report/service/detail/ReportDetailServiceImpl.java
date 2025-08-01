package com.kuit.findyou.domain.report.service.detail;


import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.ViewedReport;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ViewedReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.ReportDetailStrategy;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Service
public class ReportDetailServiceImpl implements ReportDetailService {

    private final Map<ReportTag, ReportDetailStrategy<? extends Report, ?>> strategies;
    private final ViewedReportRepository viewedReportRepository;
    private final InterestReportRepository interestReportRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <REPORT_TYPE extends Report, DTO_TYPE> DTO_TYPE getReportDetail(ReportTag tag, Long reportId, Long userId) {
        ReportDetailStrategy<REPORT_TYPE, DTO_TYPE> strategy =
                (ReportDetailStrategy<REPORT_TYPE, DTO_TYPE>) strategies.get(tag);

        REPORT_TYPE report = strategy.getReport(reportId);

        // 1. 사용자 조회
        User user = userRepository.getReferenceById(userId); // 실제 DB hit 없음

        // 2. 기존 조회 기록 삭제 (있다면)
        viewedReportRepository.deleteByUserIdAndReportId(userId, report.getId());

        // 3. 새 조회 기록 저장
        viewedReportRepository.save(ViewedReport.createViewedReport(user, report));

        // 4. 관심 여부 조회
        boolean interest = interestReportRepository.existsByReportIdAndUserId(report.getId(), userId);

        return strategy.getDetail(report, interest);
    }


}



