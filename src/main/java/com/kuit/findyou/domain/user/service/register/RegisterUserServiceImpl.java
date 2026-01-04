package com.kuit.findyou.domain.user.service.register;

import com.kuit.findyou.domain.auth.service.IssueTokenService;
import com.kuit.findyou.domain.user.dto.request.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.response.RegisterUserResponse;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegisterUserServiceImpl implements RegisterUserService {
    private final UserRepository userRepository;
    private final IssueTokenService issueTokenService;
    @Override
    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        // 카카오 Id가 중복되는 사용자가 있는지 확인
        if(userRepository.findByKakaoId(request.kakaoId()).isPresent()){
            log.info("[registerUser] user with kakaoId {} alreay exists", request.kakaoId());
            throw new CustomException(ALREADY_REGISTERED_USER);
        }

        User user = userRepository.findByDeviceId(request.deviceId())
                .map(existing -> {
                    log.info("[registerUser] user with deviceId {} alreay exists", request.deviceId());
                    existing.upgradeToMember(request.kakaoId(), request.nickname());
                    return existing;
                })
                .orElseGet(()->{
                    log.info("[registerUser] user not found");
                    return mapToUser(request);
                });

        User save = userRepository.save(user);

        // 회원가입 완료 응답하기
        String accessToken = issueTokenService.issueAccessToken(save.getId(), save.getRole());
        String refreshToken = issueTokenService.issueRefreshToken(save.getId());

        return new RegisterUserResponse(save.getId(), save.getName(), accessToken, refreshToken);
    }

    private User mapToUser(RegisterUserRequest request) {
        return User.builder()
                .kakaoId(request.kakaoId())
                .name(request.nickname())
                .role(Role.USER)
                .deviceId(request.deviceId())
                .build();
    }
}
