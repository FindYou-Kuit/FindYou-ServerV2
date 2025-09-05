package com.kuit.findyou.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateMissingReportRequest (
        @Schema(description = "업로드된 이미지 목록. cdn url 형식", example = "[\"https://cdn.findyou.store/image.jpg\"]")
        @Size(max = 5, message = "이미지는 최대 5개까지 등록할 수 있습니다.")
        List<String> imgUrls,

        @Schema(description = "축종", example = "강아지")
        @NotBlank(message = "축종은 필수 입력 항목입니다.")
        String species,

        @Schema(description = "품종", example = "말티즈")
        @NotBlank(message = "품종은 필수 입력 항목입니다.")
        String breed,

        @Schema(description = "동물의 나이", example = "3살")
        @NotBlank(message = "나이는 필수 입력 항목입니다.")
        String age,

        @Schema(description = "성별 ('남자' 또는 '여자')", example = "남자")
        @NotBlank(message = "성별은 필수 입력 항목입니다.")
        @Pattern(regexp = "^(남자|여자)$", message = "성별은 '남자' 또는 '여자'만 입력 가능합니다.")
        String sex,

        @Schema(description = "RFID/내장칩 번호", example = "9900112233445566")
        String rfid,

        @Schema(description = "털 색. 복수 요청 가능 (&로 구분)", example = "흰색")
        @NotBlank(message = "털색은 필수 입력 항목입니다.")
        String furColor,

        @Schema(description = "실종 날짜 (yyyy-MM-dd 형식)", example = "2025-09-05")
        @NotNull(message = "실종 날짜는 필수 입력 항목입니다.")
        LocalDate missingDate,

        @Schema(description = "특이 사항", example = "예쁘게 생겼음")
        String significant,

        @Schema(description = "실종 주소", example = "경기도 광명시 일직동 517")
        @NotBlank(message = "실종 주소는 필수 입력 항목입니다.")
        String location,

        @Schema(description = "주변 장소", example = "광명역")
        @NotBlank(message = "주변 장소는 필수 입력 항목입니다.")
        String landmark
){ }
