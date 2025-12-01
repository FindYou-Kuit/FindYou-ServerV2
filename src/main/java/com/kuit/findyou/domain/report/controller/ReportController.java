package com.kuit.findyou.domain.report.controller;

import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;
import com.kuit.findyou.domain.report.dto.request.CreateWitnessReportRequest;
import com.kuit.findyou.domain.report.dto.request.RetrieveReportRequestDTO;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.dto.response.WitnessReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.service.facade.ReportServiceFacade;
import com.kuit.findyou.domain.report.service.retrieve.ProtectingReportRetrieveWithS3Service;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.jwt.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.*;

@RestController
@Slf4j
@RequestMapping("/api/v2/reports")
@Tag(name = "Report", description = "글 관련 API")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportServiceFacade reportServiceFacade;
    private final ProtectingReportRetrieveWithS3Service protectingReportRetrieveWithS3Service;

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
            @Valid @ModelAttribute RetrieveReportRequestDTO request,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        CardResponseDTO result = reportServiceFacade.retrieveReportsWithFilters(request, userId);
        return BaseResponse.ok(result);
    }

    @Operation(summary = "실종 신고글 등록 API", description = "실종 신고글 등록에 필요한 내용들을 포함해 등록하는 API")
    @CustomExceptionDescription(DEFAULT)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/new-missing-reports")
    public BaseResponse<Void> createMissingReport(@Valid @RequestBody CreateMissingReportRequest request,
                                                  @Parameter(hidden = true) @LoginUserId Long userId) {
        reportServiceFacade.createMissingReport(request, userId);
        return BaseResponse.ok(null);
    }

    @Operation(summary = "목격 신고글 등록 API", description = "목격 신고글 등록에 필요한 내용들을 포함해 등록하는 API")
    @CustomExceptionDescription(DEFAULT)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/new-witness-reports")
    public BaseResponse<Void> createWitnessReport(@Valid @RequestBody CreateWitnessReportRequest request,
                                                  @Parameter(hidden = true) @LoginUserId Long userId) {
        reportServiceFacade.createWitnessReport(request, userId);
        return BaseResponse.ok(null);
    }

    @Operation(summary = "신고글 삭제 API", description = "자신이 작성한 신고글(실종/목격)을 삭제합니다.")
    @CustomExceptionDescription(DELETE_REPORT)
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{reportId}")
    public BaseResponse<Void> deleteReport(
            @Parameter(description = "삭제할 신고글의 ID") @PathVariable("reportId") Long reportId,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        reportServiceFacade.deleteReport(reportId, userId);
        return BaseResponse.ok(null);
    }

    @Operation(summary = "보호글 S3 이미지 포함 랜덤 조회 API", description = "랜덤으로 보호글을 선택하여 원본 이미지를 S3에 업로드 후, S3 URL 포함 보호글을 리스트로 반환합니다.")
    @GetMapping("/protecting-reports/random-s3")
    @CustomExceptionDescription(DEFAULT)
    public ResponseEntity<?> getRandomProtectingReportsWithS3(
            @RequestParam(name = "count", defaultValue = "1")
             @Min(1) @Max(10) int count
    ) {
        List<ProtectingReportDetailResponseDTO> details =
                protectingReportRetrieveWithS3Service.getRandomProtectingReportsWithS3(count);

        if (details.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(BaseResponse.ok(details));
    }

}

