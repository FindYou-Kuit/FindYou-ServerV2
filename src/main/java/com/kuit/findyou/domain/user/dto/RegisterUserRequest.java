package com.kuit.findyou.domain.user.dto;

import org.springframework.web.multipart.MultipartFile;

public record RegisterUserRequest(
        MultipartFile profileImage,
        String defaultProfileImageName,
        String nickname,
        Long kakaoId,
        String deviceId
){
}
