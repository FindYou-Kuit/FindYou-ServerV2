package com.kuit.findyou.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = """
        회원정보 등록 요청 DTO
        """ )
@Builder
public record RegisterUserRequest(
        @Schema(description = "회원 닉네임",
                example = "찾아유",
                required = true)
        String nickname,
        @Schema(description = "회원 카카오 ID",
                example = "123456789",
                required = true)
        Long kakaoId,
        @Schema(description = "회원 디바이스 ID",
                example = "abcd-efgh-1234",
                required = true)
        String deviceId
){
}
