package com.kuit.findyou.domain.user.dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record RegisterUserRequest(
        MultipartFile profileImage,
        String defaultProfileImageName,
        String nickname,
        Long kakaoId,
        String deviceId
){
}
