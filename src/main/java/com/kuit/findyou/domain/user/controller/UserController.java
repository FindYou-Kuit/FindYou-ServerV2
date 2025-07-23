package com.kuit.findyou.domain.user.controller;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.user.service.facade.UserServiceFacade;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.jwt.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.DEFAULT;
import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.PROTECTING_REPORT_DETAIL;

@RestController
@RequestMapping("api/v2/users")
@Tag(name = "User", description = "유저 관련 API")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceFacade userServiceFacade;

    @Operation(summary = "최근 본 글 조회 API", description = "최근 본 글을 조회하기 위한 API")
    @GetMapping("/me/viewed-reports")
    @CustomExceptionDescription(DEFAULT)
    public BaseResponse<CardResponseDTO> retrieveViewedReports (
            @RequestParam("lastId") Long lastId,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        CardResponseDTO result = userServiceFacade.retrieveViewedReports(lastId, userId);
        return BaseResponse.ok(result);
    }
}
