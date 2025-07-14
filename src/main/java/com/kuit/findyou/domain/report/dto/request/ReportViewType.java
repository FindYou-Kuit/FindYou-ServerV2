package com.kuit.findyou.domain.report.dto.request;

import lombok.Getter;

@Getter
public enum ReportViewType {
    ALL("전체 조회"),
    PROTECTING("구조 동물 조회"),
    REPORTING("신고 동물 조회");

    private final String value;

    ReportViewType(String value) {
        this.value = value;
    }
}

