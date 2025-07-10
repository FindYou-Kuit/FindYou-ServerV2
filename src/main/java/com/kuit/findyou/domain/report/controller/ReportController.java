package com.kuit.findyou.domain.report.controller;

import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.dto.response.WitnessReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.service.ReportDetailService;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.common.swagger.SwaggerResponseDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.*;

@RestController
@Slf4j
@RequestMapping("/api/v2/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportDetailService reportDetailService;

    // todo userId 를 토큰으로부터 추출하는 로직 필요
    @GetMapping("/protecting/{reportId}")
    @CustomExceptionDescription(PROTECTING_REPORT_DETAIL)
    public BaseResponse<ProtectingReportDetailResponseDTO> getProtectingReportDetail(
            @PathVariable("reportId") Long reportId) {


        ProtectingReportDetailResponseDTO detail = reportDetailService.getReportDetail(ReportTag.PROTECTING, reportId, 1L);
        return BaseResponse.ok(detail);
    }

    // todo userId 를 토큰으로부터 추출하는 로직 필요
    @GetMapping("/missing/{reportId}")
    @CustomExceptionDescription(MISSING_REPORT_DETAIL)
    public BaseResponse<MissingReportDetailResponseDTO> getMissingReportDetail(
            @PathVariable("reportId") Long reportId) {

        MissingReportDetailResponseDTO detail = reportDetailService.getReportDetail(ReportTag.MISSING, reportId, 1L);
        return BaseResponse.ok(detail);
    }

    // todo userId 를 토큰으로부터 추출하는 로직 필요
    @GetMapping("/witness/{reportId}")
    @CustomExceptionDescription(WITNESS_REPORT_DETAIL)
    public BaseResponse<WitnessReportDetailResponseDTO> getWitnessReportDetail(
            @PathVariable("reportId") Long reportId) {

        WitnessReportDetailResponseDTO detail = reportDetailService.getReportDetail(ReportTag.WITNESS, reportId, 1L);
        return BaseResponse.ok(detail);
    }
}
