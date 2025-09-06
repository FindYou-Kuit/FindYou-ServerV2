package com.kuit.findyou.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "글 조회 필터 요청")
public record RetrieveReportRequestDTO(

        @NotNull(message = "type 은 필수입니다.")
        @Schema(description = "조회 타입 (ALL / PROTECTING / REPORTING)", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALL")
        ReportViewType type,

        @Schema(description = "시작일(YYYY-MM-DD)", example = "2025-01-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @Schema(description = "종료일(YYYY-MM-DD)", example = "2025-12-31")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @Schema(description = "축종(강아지/고양이/기타)", example = "강아지")
        @Pattern(
                regexp = "^(강아지|고양이|기타)$",
                message = "species 는 강아지, 고양이, 기타 중 하나여야 합니다."
        )
        String species,

        @Schema(description = "품종(복수일 경우 콤마 구분)", example = "골든 리트리버,말티즈")
        String breeds,

        @Schema(description = "주소(키워드 포함 검색)", example = "서울 송파구")
        String address,

        @NotNull(message = "lastId는 필수입니다.")
        @Schema(description = "커서 페이징 기준 ID(처음 요청 시 0 사용)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
        Long lastId
) {}
