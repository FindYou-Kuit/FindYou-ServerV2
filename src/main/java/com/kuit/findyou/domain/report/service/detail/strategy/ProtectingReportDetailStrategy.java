package com.kuit.findyou.domain.report.service.detail.strategy;

import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.PROTECTING_REPORT_NOT_FOUND;

@RequiredArgsConstructor
@Component
public class ProtectingReportDetailStrategy implements ReportDetailStrategy<ProtectingReport, ProtectingReportDetailResponseDTO> {

    private final ProtectingReportRepository protectingReportRepository;

    private static final BigDecimal DEFAULT_COORDINATE = BigDecimal.valueOf(0.0);

    @Override
    public ProtectingReportDetailResponseDTO getDetail(ProtectingReport report, boolean interest) {
        BigDecimal latitude = report.getLatitude();
        BigDecimal longitude = report.getLongitude();

        return new ProtectingReportDetailResponseDTO(
                report.getReportImagesUrlList(),
                report.getBreed(),
                report.getTag().getValue(),
                report.getAge().equals("미상") ? "미상" : report.getAge() + "살",
                report.getWeight().equals("미상") ? "미상" : report.getWeight() + "kg",
                report.getFurColor(),
                report.getSex().getValue(),
                report.getNeutering().toString(),
                report.getSignificant(),
                report.getCareName(),
                report.getAddress(),
                (latitude == null || latitude.equals(DEFAULT_COORDINATE)) ? null : latitude.doubleValue(),
                (longitude == null || longitude.equals(DEFAULT_COORDINATE)) ? null : longitude.doubleValue(),
                report.getCareTel(),
                report.getDate().toString(),
                report.getFoundLocation(),
                report.getNoticeDuration(),
                report.getNoticeNumber(),
                report.getAuthority(),
                interest
        );
    }


    @Override
    public ProtectingReport getReport(Long reportId) {
        return protectingReportRepository.findWithImagesById(reportId)
                .orElseThrow(() -> new CustomException(PROTECTING_REPORT_NOT_FOUND));
    }
}
