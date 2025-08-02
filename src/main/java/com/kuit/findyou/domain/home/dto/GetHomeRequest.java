package com.kuit.findyou.domain.home.dto;

import com.kuit.findyou.domain.home.validation.ValidGetHomeRequest;

@ValidGetHomeRequest
public record GetHomeRequest(
        Double lat,
        Double lng
) {
}
