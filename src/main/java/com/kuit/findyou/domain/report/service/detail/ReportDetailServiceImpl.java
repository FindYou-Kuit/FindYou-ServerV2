package com.kuit.findyou.domain.report.service.detail;


import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.strategy.ReportDetailStrategy;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class ReportDetailServiceImpl implements ReportDetailService {

    private final InterestReportRepository interestReportRepository;

    private final Map<String, ReportDetailStrategy<? extends Report, ?>> strategies;

    @SuppressWarnings("unchecked")
    public <REPORT_TYPE extends Report, DTO_TYPE> DTO_TYPE getReportDetail(ReportTag tag, Long reportId, Long userId) {

        ReportDetailStrategy<REPORT_TYPE, DTO_TYPE> strategy =  (ReportDetailStrategy<REPORT_TYPE, DTO_TYPE>) strategies.get(tag.toString());

        if (strategy == null) {
            throw new CustomException(ILLEGAL_TAG);
        }

        REPORT_TYPE report = strategy.getReport(reportId);
        boolean interest = interestReportRepository.existsByReport_IdAndUser_Id(report.getId(), userId);

        return strategy.getDetail(report, interest);
    }

}
