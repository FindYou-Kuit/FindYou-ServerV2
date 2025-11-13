package com.kuit.findyou.global.common.util;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.MultiPartSpecification;

import java.nio.charset.StandardCharsets;

public class RestAssuredUtils {
    public static MultiPartSpecification multipartText(String controlName, String textBody){
        return new MultiPartSpecBuilder(textBody)
                .controlName(controlName)
                .mimeType(String.valueOf(ContentType.TEXT))
                .charset(StandardCharsets.UTF_8)
                .build();
    }
}
