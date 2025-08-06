package com.kuit.findyou.domain.report.service.detail.strategy;

import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.domain.report.util.ReportFormatUtil;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.PROTECTING_REPORT_NOT_FOUND;

@RequiredArgsConstructor
@Component
public class ProtectingReportDetailStrategy implements ReportDetailStrategy<ProtectingReport, ProtectingReportDetailResponseDTO> {

    private final ProtectingReportRepository protectingReportRepository;

    @Override
    public ProtectingReportDetailResponseDTO getDetail(ProtectingReport report, boolean interest) {

        return new ProtectingReportDetailResponseDTO(
                report.getReportImagesUrlList(),
                report.getBreed(),
                report.getTag().getValue(),
                ReportFormatUtil.formatAge(report.getAge()),
                ReportFormatUtil.formatWeight(report.getWeight()),
                report.getFurColor(),
                report.getSex().getValue(),
                report.getNeutering().toString(),
                report.getSignificant(),
                report.getCareName(),
                report.getAddress(),
                ReportFormatUtil.formatCoordinate(report.getLatitude()),
                ReportFormatUtil.formatCoordinate(report.getLongitude()),
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
