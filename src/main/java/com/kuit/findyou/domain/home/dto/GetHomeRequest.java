package com.kuit.findyou.domain.home.dto;

import com.kuit.findyou.domain.home.validation.ValidGetHomeRequest;
import io.swagger.v3.oas.annotations.Parameter;

@ValidGetHomeRequest
public record GetHomeRequest(
        @Parameter Double lat,
        @Parameter Double lng
) {
}
