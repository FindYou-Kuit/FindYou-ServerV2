package com.kuit.findyou.domain.inquiry.controller;

import com.kuit.findyou.domain.inquiry.dto.AddInquiryRequest;
import com.kuit.findyou.domain.inquiry.service.InquiryService;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.jwt.annotation.LoginUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v2/inquiries")
@RequiredArgsConstructor
@RestController
public class InquiryController {
    private final InquiryService inquiryService;
    @PostMapping
    public BaseResponse<Void> addInquiry(@LoginUserId Long userId, @RequestBody @Valid AddInquiryRequest request){
        inquiryService.addInquiry(userId, request);
        return BaseResponse.ok(null);
    }
}
