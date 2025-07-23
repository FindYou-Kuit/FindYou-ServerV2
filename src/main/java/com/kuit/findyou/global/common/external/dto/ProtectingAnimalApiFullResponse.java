package com.kuit.findyou.global.common.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProtectingAnimalApiFullResponse(
        ProtectingAnimalApiResponse response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProtectingAnimalApiResponse(
            ProtectingAnimalHeader header,
            ProtectingAnimalBody body
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProtectingAnimalHeader(
            String reqNo,
            String resultCode,
            String resultMsg
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProtectingAnimalBody(
            ProtectingAnimalItems items,
            String numOfRows,
            String pageNo,
            String totalCount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProtectingAnimalItems(
            List<ProtectingAnimalItemDTO> item
    ) {}
}

