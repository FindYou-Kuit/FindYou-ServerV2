package com.kuit.findyou.domain.report.service.detail;

import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.service.detail.strategy.ReportDetailStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CoordinateUpdateService {

    private final Map<ReportTag, ReportDetailStrategy<? extends Report, ?>> strategies;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @SuppressWarnings("unchecked")
    public void updateCoordinates(ReportTag tag, Long reportId, BigDecimal latitude, BigDecimal longitude) {
        ReportDetailStrategy<Report, ?> strategy = (ReportDetailStrategy<Report, ?>) strategies.get(tag);

        Report report = strategy.getReport(reportId);

        if (report.isCoordinatesAbsent()) {
            report.setLatitude(latitude);
            report.setLongitude(longitude);
        }
    }
}

