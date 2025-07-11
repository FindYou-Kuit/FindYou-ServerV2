package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.MISSING_REPORT_NOT_FOUND;

@RequiredArgsConstructor
@Component("MISSING")
public class MissingReportDetailStrategy implements ReportDetailStrategy<MissingReport, MissingReportDetailResponseDTO> {

    private final MissingReportRepository missingReportRepository;

    @Override
    public MissingReportDetailResponseDTO getDetail(MissingReport report, boolean interest) {
        return new MissingReportDetailResponseDTO(
                report.getReportImagesUrlList(),
                report.getBreed(),
                report.getTag().getValue(),
                report.getAge(),
                report.getSex().getValue(),
                report.getDate().toString(),
                report.getRfid(),
                report.getSignificant(),
                report.getLandmark(),       // missingLocation
                report.getAddress(),        // missingAddress
                report.getLatitude().doubleValue(),
                report.getLongitude().doubleValue(),
                report.getReporterName(),
                report.getReporterTel(),
                interest
        );
    }

    @Override
    public MissingReport getReport(Long reportId) {
        return missingReportRepository.findWithImagesById(reportId)
                .orElseThrow(() -> new CustomException(MISSING_REPORT_NOT_FOUND));
    }
}

