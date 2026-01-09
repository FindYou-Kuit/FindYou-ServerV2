package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;

import java.util.List;

public interface ProtectingReportRetrieveWithS3Service {
    List<ProtectingReportDetailResponseDTO> getRandomProtectingReportsWithS3(int count);
}
