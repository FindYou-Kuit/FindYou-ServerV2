package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.dto.response.WitnessReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.WitnessReport;
import org.springframework.stereotype.Component;

@Component("WITNESS")
public class WitnessReportDetailStrategy implements ReportDetailStrategy<WitnessReport, WitnessReportDetailResponseDTO> {

    @Override
    public WitnessReportDetailResponseDTO getDetail(WitnessReport report, boolean interest) {
        return new WitnessReportDetailResponseDTO(
                report.getReportImagesUrlList(),
                report.getBreed(),
                report.getTag().getValue(),
                report.getFurColor(),
                report.getSignificant(),
                report.getLandmark(),       // witnessLocation
                report.getAddress(),        // witnessAddress
                report.getLatitude().doubleValue(),
                report.getLongitude().doubleValue(),
                report.getReporterName(),
                report.getDate().toString(),
                interest
        );
    }
}

