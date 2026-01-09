package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;

import java.util.List;

public interface MissingReportRetrieveWithS3Service {
    List<MissingReportDetailResponseDTO> getRandomMissingReportsWithS3(int count);
}
