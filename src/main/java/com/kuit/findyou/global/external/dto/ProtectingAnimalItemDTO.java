package com.kuit.findyou.global.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record ProtectingAnimalItemDTO(
        String happenDt,
        String happenPlace,
        String upKindNm,
        String kindNm,
        String colorCd,
        String age,
        String weight,
        String noticeNo,
        String noticeSdt,
        String noticeEdt,
        String popfile1,
        String popfile2,
        String sexCd,
        String neuterYn,
        String specialMark,
        String careNm,
        String careTel,
        String careAddr,
        String careOwnerNm,
        String orgNm
) {}

