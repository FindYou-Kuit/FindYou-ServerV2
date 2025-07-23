package com.kuit.findyou.domain.report.service.detail;


import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.ViewedReport;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ViewedReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.ReportDetailStrategy;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.external.client.KakaoCoordinateClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Map;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;


@Slf4j
@RequiredArgsConstructor
@Service
public class ReportDetailServiceImpl implements ReportDetailService {

    private final Map<ReportTag, ReportDetailStrategy<? extends Report, ?>> strategies;
    private final ViewedReportRepository viewedReportRepository;
    private final InterestReportRepository interestReportRepository;
    private final KakaoCoordinateClient kakaoCoordinateClient;
    private final CoordinateUpdateService coordinateUpdateService;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 2. 기존 조회 기록 삭제 (있다면)
        viewedReportRepository.deleteByUserIdAndReportId(userId, report.getId());

        // 3. 새 조회 기록 저장
        viewedReportRepository.save(ViewedReport.createViewedReport(user, report));

        // 4. 관심 여부 조회
        boolean interest = interestReportRepository.existsByReportIdAndUserId(report.getId(), userId);

        // 5. 좌표가 없으면 갱신 시도
        if (report.isCoordinatesAbsent()) {
            var coordinate = kakaoCoordinateClient.getCoordinatesFromAddress(report.getAddress());

            try {
                coordinateUpdateService.updateCoordinates(tag, reportId, coordinate.latitude(), coordinate.longitude());
                log.info("[좌표 갱신 성공]");
            } catch (OptimisticLockingFailureException | OptimisticLockException e) {
                log.warn("[좌표 갱신 실패]");
            }
        }

        // 6. DB에서 최신 좌표값 다시 조회
        em.refresh(report);

        return strategy.getDetail(report, interest);
    }


}



