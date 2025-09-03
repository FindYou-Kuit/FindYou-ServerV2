package com.kuit.findyou.domain.report.controller;

import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;
import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.dto.response.WitnessReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.report.service.facade.ReportServiceFacade;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.jwt.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.*;

@RestController
@Slf4j
@RequestMapping("/api/v2/reports")
@Tag(name = "Report", description = "글 관련 API")
@RequiredArgsConstructor
public class ReportController {

    private final ReportServiceFacade reportServiceFacade;

    @Operation(summary = "보호글 상세 조회 API", description = "보호글의 정보를 상세 조회하기 위한 API")
    @GetMapping("/protecting-reports/{reportId}")
    @CustomExceptionDescription(PROTECTING_REPORT_DETAIL)
    public BaseResponse<ProtectingReportDetailResponseDTO> getProtectingReportDetail(
            @PathVariable("reportId") Long reportId,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        ProtectingReportDetailResponseDTO detail = reportServiceFacade.getReportDetail(ReportTag.PROTECTING, reportId, userId);
        return BaseResponse.ok(detail);
    }

    @Operation(summary = "실종 신고글 상세 조회 API", description = "실종 신고글의 정보를 상세 조회하기 위한 API")
    @GetMapping("/missing-reports/{reportId}")
    @CustomExceptionDescription(MISSING_REPORT_DETAIL)
    public BaseResponse<MissingReportDetailResponseDTO> getMissingReportDetail(
            @PathVariable("reportId") Long reportId,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        MissingReportDetailResponseDTO detail = reportServiceFacade.getReportDetail(ReportTag.MISSING, reportId, userId);
        return BaseResponse.ok(detail);
    }

    @Operation(summary = "목격 신고글 상세 조회 API", description = "목격 신고글의 정보를 상세 조회하기 위한 API")
    @GetMapping("/witness-reports/{reportId}")
    @CustomExceptionDescription(WITNESS_REPORT_DETAIL)
    public BaseResponse<WitnessReportDetailResponseDTO> getWitnessReportDetail(
            @PathVariable("reportId") Long reportId,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        WitnessReportDetailResponseDTO detail = reportServiceFacade.getReportDetail(ReportTag.WITNESS, reportId, userId);
        return BaseResponse.ok(detail);
    }

    @Operation(summary = "글 조회 API (전체 / 구조 동물 / 신고 동물)", description = "글 조회를 위한 API - 전체 조회/구조 동물 조회/신고 동물 조회 시 쿼리 파라미터로 케이스를 구분")
    @GetMapping
    @CustomExceptionDescription(DEFAULT)
    public BaseResponse<CardResponseDTO> retrieveReportsWithFilters(
            @RequestParam ReportViewType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String breeds,
            @RequestParam(required = false) String address,
            @RequestParam Long lastId,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        CardResponseDTO result = reportServiceFacade.retrieveReportsWithFilters(type, startDate, endDate, species, breeds, address, lastId, userId);
        return BaseResponse.ok(result);
    }

    @Operation(summary = "실종 신고글 등록 API", description = "실종 신고글 등록에 필요한 내용들을 포함해 등록하는 API")
    @CustomExceptionDescription(DEFAULT)
    @PostMapping("/new-missing-reports")
    public BaseResponse<Void> createMissingReport(@RequestBody CreateMissingReportRequest request,
                                                  @Parameter(hidden = true) @LoginUserId Long userId) {
        reportServiceFacade.createMissingReport(request, userId);
        return BaseResponse.ok(null);
    }

}

