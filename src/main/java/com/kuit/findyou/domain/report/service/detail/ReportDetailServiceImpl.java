package com.kuit.findyou.domain.report.service.detail;


import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.strategy.ReportDetailStrategy;
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


@Slf4j
@RequiredArgsConstructor
@Service
public class ReportDetailServiceImpl implements ReportDetailService {

    private final Map<ReportTag, ReportDetailStrategy<? extends Report, ?>> strategies;
    private final InterestReportRepository interestReportRepository;
    private final KakaoCoordinateClient kakaoCoordinateClient;
    private final CoordinateUpdateService coordinateUpdateService;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <REPORT_TYPE extends Report, DTO_TYPE> DTO_TYPE getReportDetail(ReportTag tag, Long reportId, Long userId) {
        ReportDetailStrategy<REPORT_TYPE, DTO_TYPE> strategy =
                (ReportDetailStrategy<REPORT_TYPE, DTO_TYPE>) strategies.get(tag);

        REPORT_TYPE report = strategy.getReport(reportId);
        boolean interest = interestReportRepository.existsByReportIdAndUserId(report.getId(), userId);

        if (report.isCoordinatesAbsent()) {
            var coordinate = kakaoCoordinateClient.getCoordinatesFromAddress(report.getAddress());

            try {
                coordinateUpdateService.updateCoordinates(tag, reportId, coordinate.latitude(), coordinate.longitude());
            } catch (OptimisticLockingFailureException | OptimisticLockException e1) {
                log.warn("[좌표 갱신 실패 1차] reportId={} - 재시도 시도", reportId);
                try {
                    coordinateUpdateService.updateCoordinates(tag, reportId, coordinate.latitude(), coordinate.longitude());
                } catch (OptimisticLockingFailureException | OptimisticLockException e2) {
                    log.warn("[좌표 갱신 실패 2차] reportId={} - 좌표 없이 응답", reportId);
                }
            }

            em.refresh(report);
        }

        return strategy.getDetail(report, interest);
    }

}



