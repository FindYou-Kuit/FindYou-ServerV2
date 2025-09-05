package com.kuit.findyou.domain.inquiry.controller;

import com.kuit.findyou.domain.inquiry.dto.AddInquiryRequest;
import com.kuit.findyou.domain.inquiry.service.InquiryService;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.jwt.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.DEFAULT;

@Tag(name = "Inquiry", description = "문의사항 관련 API")
@RequestMapping("/api/v2/inquiries")
@RequiredArgsConstructor
@RestController
public class InquiryController {
    private final InquiryService inquiryService;
    @Operation(
            summary = "문의사항 추가 API",
            description = "문의사항 추가 기능을 수행합니다."
    )
    @CustomExceptionDescription(DEFAULT)
    @PostMapping
    public BaseResponse<Void> addInquiry(@Parameter(hidden = true) @LoginUserId Long userId,
                                         @RequestBody @Valid AddInquiryRequest request){
        inquiryService.addInquiry(userId, request);
        return BaseResponse.ok(null);
    }
}
