package com.kuit.findyou.domain.user.model;

import lombok.Getter;

@Getter
public enum Role {

    USER("회원"), GUEST("비회원");

    private final String value;

    Role(String value) {
        this.value = value;
    }
}
