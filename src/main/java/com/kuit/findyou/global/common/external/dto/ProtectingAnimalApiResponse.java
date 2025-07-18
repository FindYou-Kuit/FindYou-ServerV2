package com.kuit.findyou.global.common.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProtectingAnimalApiResponse(
        ProtectingAnimalBody body
) {

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
