package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.dto.response.WitnessReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.WitnessReport;
import com.kuit.findyou.domain.report.repository.WitnessReportRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.WITNESS_REPORT_NOT_FOUND;

@RequiredArgsConstructor
@Component
public class WitnessReportDetailStrategy implements ReportDetailStrategy<WitnessReport, WitnessReportDetailResponseDTO> {

    private final WitnessReportRepository witnessReportRepository;

    private static final BigDecimal DEFAULT_COORDINATE = BigDecimal.valueOf(0.0);

    @Override
    public WitnessReportDetailResponseDTO getDetail(WitnessReport report, boolean interest) {
        BigDecimal latitude = report.getLatitude();
        BigDecimal longitude = report.getLongitude();

        return new WitnessReportDetailResponseDTO(
                report.getReportImagesUrlList(),
                report.getBreed(),
                report.getTag().getValue(),
                report.getFurColor(),
                report.getSignificant(),
                report.getLandmark(),       // witnessLocation
                report.getAddress(),        // witnessAddress
                (latitude == null || latitude.equals(DEFAULT_COORDINATE)) ? null : latitude.doubleValue(),
                (longitude == null || longitude.equals(DEFAULT_COORDINATE)) ? null : longitude.doubleValue(),
                report.getReporterName(),
                report.getDate().toString(),
                interest
        );
    }

    @Override
    public WitnessReport getReport(Long reportId) {
        return witnessReportRepository.findWithImagesById(reportId)
                .orElseThrow(() -> new CustomException(WITNESS_REPORT_NOT_FOUND));
    }
}

