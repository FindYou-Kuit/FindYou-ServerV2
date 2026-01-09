package com.kuit.findyou.domain.auth.dto;

public record ReissueTokenRequest(
        String refreshToken
) {
}
