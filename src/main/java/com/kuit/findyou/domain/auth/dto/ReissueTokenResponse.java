package com.kuit.findyou.domain.auth.dto;

public record ReissueTokenResponse(
        String accessToken,
        String refreshToken
) {
}
