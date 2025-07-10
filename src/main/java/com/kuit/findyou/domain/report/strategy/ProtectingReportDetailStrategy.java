package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.model.Report;
import org.springframework.stereotype.Component;

@Component("PROTECTING")
public class ProtectingReportDetailStrategy implements ReportDetailStrategy<ProtectingReport, ProtectingReportDetailResponseDTO> {

    @Override
    public ProtectingReportDetailResponseDTO getDetail(ProtectingReport report, boolean interest) {
        return new ProtectingReportDetailResponseDTO(
                report.getReportImagesUrlList(),
                report.getBreed(),
                report.getTag().getValue(),
                report.getAge(),
                report.getWeight(),
                report.getFurColor(),
                report.getSex().getValue(),
                report.getNeutering().toString(),
                report.getSignificant(),
                report.getCareName(),
                report.getAddress(),
                report.getLatitude().doubleValue(),
                report.getLongitude().doubleValue(),
                report.getCareTel(),
                report.getDate().toString(),
                report.getFoundLocation(),
                report.getNoticeDuration(),
                report.getNoticeNumber(),
                report.getAuthority(),
                interest
        );
    }
}
