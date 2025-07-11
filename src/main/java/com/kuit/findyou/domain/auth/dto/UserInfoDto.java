package com.kuit.findyou.domain.auth.dto;

public record UserInfoDto(
        Long userId,
        String nickname,
        String accessToken
) {
}
