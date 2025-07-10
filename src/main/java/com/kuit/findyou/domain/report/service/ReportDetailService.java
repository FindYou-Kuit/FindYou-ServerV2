package com.kuit.findyou.domain.report.service;


import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.domain.report.repository.WitnessReportRepository;
import com.kuit.findyou.domain.report.strategy.ReportDetailStrategy;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class ReportDetailService {

    private final ProtectingReportRepository protectingReportRepository;
    private final MissingReportRepository missingReportRepository;
    private final WitnessReportRepository witnessReportRepository;

    private final InterestReportRepository interestReportRepository;

    private final Map<String, ReportDetailStrategy<? extends Report, ?>> strategies;

    @SuppressWarnings("unchecked")
    public <T> T getReportDetail(ReportTag tag, Long reportId, Long userId) {
        ReportDetailStrategy<Report, T> strategy =  (ReportDetailStrategy<Report, T>) strategies.get(tag.toString());

        if (strategy == null) {
            throw new CustomException(ILLEGAL_TAG);
        }

        Report report = findReportByTagAndId(tag, reportId);
        boolean interest = interestReportRepository.existsByReport_IdAndUser_Id(report.getId(), userId);

        return strategy.getDetail(report, interest);
    }

    private Report findReportByTagAndId(ReportTag tag, Long reportId) {
        return switch (tag) {
            case PROTECTING -> protectingReportRepository.findWithImagesById(reportId)
                    .orElseThrow(() -> new CustomException(PROTECTING_REPORT_NOT_FOUND));
            case MISSING -> missingReportRepository.findWithImagesById(reportId)
                    .orElseThrow(() -> new CustomException(MISSING_REPORT_NOT_FOUND));
            case WITNESS -> witnessReportRepository.findWithImagesById(reportId)
                    .orElseThrow(() -> new CustomException(WITNESS_REPORT_NOT_FOUND));
            default -> throw new CustomException(ILLEGAL_TAG);
        };
    }
}
