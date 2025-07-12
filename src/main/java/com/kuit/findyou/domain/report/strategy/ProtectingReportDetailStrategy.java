package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

    @Override
    public ProtectingReport getReport(Long reportId) {
        return protectingReportRepository.findWithImagesById(reportId)
                .orElseThrow(() -> new CustomException(PROTECTING_REPORT_NOT_FOUND));
    }
}
