package com.kuit.findyou.domain.home.dto;

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
            String regoin,
            @JsonProperty("prcsNm")
            String prcesssName,
            @JsonProperty("tot")
            String total
    ){ }
}
