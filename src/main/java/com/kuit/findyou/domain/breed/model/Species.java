package com.kuit.findyou.domain.breed.model;

import lombok.Getter;

@Getter
public enum Species {

    DOG("개"), CAT("고양이"), ETC("기타");

    private final String value;

    Species(String value) {
        this.value = value;
    }
}
