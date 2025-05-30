package com.kuit.findyou.domain.report.model;

import lombok.Getter;

@Getter
public enum ReportTag {
    PROTECTING("보호중"), MISSING("실종신고"), WITNESS("목격신고");

    private final String value;

    ReportTag(String value){
        this.value = value;
    }
}
