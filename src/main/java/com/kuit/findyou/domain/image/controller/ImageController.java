package com.kuit.findyou.domain.image.controller;

import com.kuit.findyou.domain.image.dto.ReportImageResponse;
import com.kuit.findyou.domain.image.service.ReportImageUploadService;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.jwt.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.DEFAULT;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("api/v2/reports/images")
@RequiredArgsConstructor
public class ImageController {
    private final ReportImageUploadService imageUploadService;

    @Operation(summary = "신고글 이미지 업로드 API", description = "멀티파트 이미지 업로드 후 CDN URL 리스트 반환")
    @CustomExceptionDescription(DEFAULT)
    @PostMapping(value = "/upload", consumes = MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<ReportImageResponse> uploadImages(@RequestPart(value = "files", required = false) List<MultipartFile> files, @LoginUserId Long userId) {
        List<String> urls = imageUploadService.uploadImages(files);
        return BaseResponse.ok(new ReportImageResponse(urls));//빈 열도 200 ok로 취급
    }
}
