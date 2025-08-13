package com.kuit.findyou.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangeNicknameRequestDTO(

        @Schema(
                description = "변경할 새 닉네임",
                example = "찾아유"
        )
        @NotBlank(message = "새로운 닉네임을 반드시 입력해야 합니다.")
        String newNickname
) {
}
