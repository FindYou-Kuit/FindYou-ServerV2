package com.kuit.findyou.domain.user.model;

import lombok.Getter;

@Getter
public enum Role {

    USER("회원"), NON_USER("비회원");

    private final String value;

    Role(String value) {
        this.value = value;
    }
}
