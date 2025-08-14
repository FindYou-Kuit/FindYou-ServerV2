package com.kuit.findyou.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangeNicknameRequestDTO(

        @Schema(
                description = "변경할 새 닉네임",
                example = "찾아유"
        )
        @NotBlank(message = "닉네임이 비어있어요.")
        @Size(max = 8, message = "닉네임은 최대 8글자까지만 가능해요.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9 ]*$",
                message = "특수문자는 들어갈 수 없어요."
        )
        String newNickname
) {
}
