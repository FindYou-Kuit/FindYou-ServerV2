package com.kuit.findyou.global.external.dto;

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
