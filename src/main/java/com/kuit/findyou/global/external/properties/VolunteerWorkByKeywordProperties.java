package com.kuit.findyou.global.external.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openapi.volunteer-work")
public record VolunteerWorkByKeywordProperties(
        String apiUrl
){

}
