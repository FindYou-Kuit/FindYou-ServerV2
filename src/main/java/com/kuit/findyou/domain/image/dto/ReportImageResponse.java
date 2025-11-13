package com.kuit.findyou.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "이미지 업로드 성공 후 응답 DTO")
public record ReportImageResponse(
        @Schema(description = "cdn url 리스트", example = "https://cdn.findyou.store/202506301036358.jpg")
        List<String> urls
) {}
