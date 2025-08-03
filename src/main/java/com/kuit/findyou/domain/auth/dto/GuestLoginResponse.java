package com.kuit.findyou.domain.auth.dto;

public record GuestLoginResponse (
        Long userId,
        String accessToken
){
}
