package com.kuit.findyou.domain.home.dto;

public record LossInfoServiceApiResponse (
   Response response
){
    public record Response(
            Body body
    ){ }

    public record Body(
            String totalCount
    ){}
}
