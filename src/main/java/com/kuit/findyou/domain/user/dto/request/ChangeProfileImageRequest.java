package com.kuit.findyou.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public record ChangeProfileImageRequest(
        @Schema(description = "사용자 업로드 이미지", type = "string", format = "binary")
        MultipartFile profileImageFile,
        @Schema(description = "기본 프로필 이미지 이름(enum string) <default, puppy, chick, panda>", example = "puppy")
        String defaultProfileImageName
) {
}
