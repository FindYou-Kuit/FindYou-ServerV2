package com.kuit.findyou.domain.user.controller;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.user.dto.*;
import com.kuit.findyou.domain.user.service.facade.UserServiceFacade;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.jwt.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.DEFAULT;

import lombok.extern.slf4j.Slf4j;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.CHECK_DUPLICATE_NICKNAME;
import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.REGISTER_USER;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@RequestMapping("api/v2/users")
@Tag(name = "User", description = "유저 관련 API")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceFacade userServiceFacade;

    @GetMapping("/me/interest-animals")
    public BaseResponse<RetrieveInterestAnimalsResponse> retrieveInterestAnimals(@LoginUserId Long userId, @RequestParam Long lastId){
        RetrieveInterestAnimalsResponse result = userServiceFacade.retrieveInterestAnimals(userId, lastId);
        return BaseResponse.ok(result);
    }

    @Operation(summary = "최근 본 동물 조회 API", description = "최근 본 동물을 조회하기 위한 API")
    @GetMapping("/me/viewed-animals")
    @CustomExceptionDescription(DEFAULT)
    public BaseResponse<CardResponseDTO> retrieveViewedAnimals (
            @RequestParam("lastId") Long lastId,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        CardResponseDTO result = userServiceFacade.retrieveViewedAnimals(lastId, userId);
        return BaseResponse.ok(result);
    }

    @Operation(
            summary = "회원정보 등록 API",
            description = """
                    회원 정보를 등록합니다. 회원 등록에 성공하면 유저 정보(식별자와 닉네임)와 엑세스 토큰을 얻을 수 있습니다. \n
                    **[중요] profileImageFile과 defaultProfileImageName 중 하나만 선택해야 합니다.** \n                 
                    - profileImageFile을 업로드하면 defaultProfileImageName은 무시됩니다. \n           
                    - 둘 다 null이면 에러가 발생합니다.
                    """
    )
    @CustomExceptionDescription(REGISTER_USER)
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<RegisterUserResponse> registerUser(@ModelAttribute RegisterUserRequest request){
        log.info("[registerUser] kakaoId = {}", request.kakaoId());
        return new BaseResponse<>(userServiceFacade.registerUser(request));
    }

    @Operation(
            summary = "닉네임 중복 확인 API",
            description = "닉네임 중복 여부를 확인합니다."
    )
    @CustomExceptionDescription(CHECK_DUPLICATE_NICKNAME)
    @PostMapping("/check/duplicate-nickname")
    public BaseResponse<CheckDuplicateNicknameResponse> checkDuplicateNickname(CheckDuplicateNicknameRequest request){
        log.info("[checkDuplicateNickname] nickname = {}", request.nickname());
        return new BaseResponse<>(userServiceFacade.checkDuplicateNickname(request));
    }
}
