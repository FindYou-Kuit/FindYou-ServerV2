package com.kuit.findyou.domain.user.constant;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DefaultProfileImage {
    DEFAULT("default"), PUPPY("puppy"), CHICK("chick"), PANDA("panda");

    private String name;

    private DefaultProfileImage(String name){
        this.name = name;
    }

    public static boolean validate(String name){
        if(name == null) return false;
        return Arrays.stream(DefaultProfileImage.values())
                .anyMatch(image -> image.getName().equalsIgnoreCase(name));
    }
}
