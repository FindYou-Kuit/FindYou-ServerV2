package com.kuit.findyou.global.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RescueAnimalStatsServiceApiResponse(
        Response response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            Body body
    ){ }

    @JsonIgnoreProperties(ignoreUnknown = true)
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
