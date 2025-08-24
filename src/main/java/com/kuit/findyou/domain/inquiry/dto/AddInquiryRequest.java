package com.kuit.findyou.domain.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Schema(description = "문의사항 추가 요청 DTO")
public record AddInquiryRequest(
        @Schema(description = "제목", example = "버그 신고합니다")
        @Size(max=300)
        @NotBlank String title,

        @Schema(description = "내용", example = "홈버튼이 안 눌립니다")
        @NotBlank String content,

        @Schema(description = "카테고리 목록", example = "[ \"오류/버그 신고\", \"개선 및 피드백\" ]")
        @NotEmpty List<@NotBlank String> categories
) {
}
