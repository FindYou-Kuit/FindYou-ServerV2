package com.kuit.findyou.domain.report.model;

import lombok.Getter;

@Getter
public enum Sex {
    M("수컷"), F("암컷"), N("중성화"), Q("미상");

    private final String value;

    Sex(String value) {
        this.value = value;
    }
}
