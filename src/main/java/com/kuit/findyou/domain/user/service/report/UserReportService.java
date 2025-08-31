package com.kuit.findyou.domain.user.service.report;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface UserReportService {
    CardResponseDTO retrieveUserReports(Long userId, Long lastId, int size);
}
