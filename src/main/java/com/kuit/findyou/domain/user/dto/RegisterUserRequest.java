package com.kuit.findyou.domain.user.dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record RegisterUserRequest(
        MultipartFile profileImageFile,
        String defaultProfileImageName,
        String nickname,
        Long kakaoId,
        String deviceId
){
}
