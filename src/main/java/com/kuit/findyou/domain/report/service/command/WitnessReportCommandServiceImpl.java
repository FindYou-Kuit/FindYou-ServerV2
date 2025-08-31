package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.report.dto.request.CreateWitnessReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WitnessReportCommandServiceImpl implements WitnessReportCommandService {
    @Override
    public void createWitnessReport(CreateWitnessReportRequest req, Long userId) {

    }
}
