package com.kuit.findyou.global.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MissingAnimalApiFullResponse(
        MissingAnimalApiResponse response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MissingAnimalApiResponse(
            MissingAnimalHeader header,
            MissingAnimalBody body
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MissingAnimalHeader(
            String reqNo,
            String resultCode,
            String resultMsg,
            String errorMsg
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MissingAnimalBody(
            MissingAnimalItems items,
            String numOfRows,
            String pageNo,
            String totalCount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MissingAnimalItems(
            List<MissingAnimalItemDTO> item
    ) {}
}