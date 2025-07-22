package com.kuit.findyou.domain.user.dto;

public record RegisterUserResponse(
        Long userId,
        String nickname,
        String accessToken
) {
}
