package com.kuit.findyou.domain.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddInquiryRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotEmpty List<@NotBlank String> categories
) {
}
