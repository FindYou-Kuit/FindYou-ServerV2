package com.kuit.findyou.global.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RescueAnimalStatsServiceApiResponse(
        Response response
) {
    public record Response(
            Body body
    ){ }

    public record Body(
            Items items
    ) { }

    public record Items(
            List<Item> item
    ){ }

    public record Item(
            @JsonProperty("se")
            String section,
            @JsonProperty("rgn")
            String region,
            @JsonProperty("prcsNm")
            String processName,
            @JsonProperty("tot")
            String total
    ){ }
}
