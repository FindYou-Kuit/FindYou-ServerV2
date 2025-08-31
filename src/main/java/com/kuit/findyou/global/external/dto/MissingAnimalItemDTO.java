package com.kuit.findyou.global.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MissingAnimalItemDTO(
        String rfidCd,
        String callName,
        String callTel,
        String happenDt,
        String happenAddr,
        String happenAddrDtl,
        String happenPlace,
        String orgNm,
        String popfile,
        String kindCd,
        String colorCd,
        String sexCd,
        String age,
        String specialMark
) {}