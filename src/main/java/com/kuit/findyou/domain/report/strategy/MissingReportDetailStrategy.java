package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.MissingReport;
import org.springframework.stereotype.Component;

@Component("MISSING")
public class MissingReportDetailStrategy implements ReportDetailStrategy<MissingReport, MissingReportDetailResponseDTO> {

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
}

