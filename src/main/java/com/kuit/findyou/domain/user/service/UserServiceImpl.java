package com.kuit.findyou.domain.user.service;

import com.kuit.findyou.domain.user.dto.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.RegisterUserResponse;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.domain.user.util.DefaultImageUrlProvider;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final ImageUploader imageUploader;
    private final DefaultImageUrlProvider defaultImageUrlProvider;
    private final JwtUtil jwtUtil;

    @Override
    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        // 카카오 Id가 중복되는 사용자가 있는지 확인
        if(userRepository.findByKakaoId(request.kakaoId()).isPresent()){
            throw new CustomException(ALREADY_SIGNED_UP_USER);
        }

        // 비회원이었는지 확인한 후에 회원 정보 저장
        String profileImageUrl = getProfileImageUrl(request);

        User user = userRepository.findByDeviceId(request.deviceId())
                .map(existing -> {
                    existing.upgradeToUser(request.kakaoId(), request.nickname(), profileImageUrl);
                    return existing;
                })
                .orElseGet(()->{
                    return createUser(request, profileImageUrl);
                });

        User save = userRepository.save(user);

        // 회원가입 완료 응답하기
        String accessToken = jwtUtil.createAccessJwt(save.getId(), save.getRole());
        return new RegisterUserResponse(save.getId(), save.getName(), accessToken);
    }

    private User createUser(RegisterUserRequest request, String profileImageUrl) {
        return User.builder()
                .kakaoId(request.kakaoId())
                .name(request.nickname())
                .profileImageUrl(profileImageUrl)
                .role(Role.USER)
                .deviceId(request.deviceId())
                .build();
    }

    private String getProfileImageUrl(RegisterUserRequest request) {
        // 인프라에 이미지 업로드
        if(isNotEmtpyProfileImage(request)){
            try{
                return imageUploader.upload(request.profileImage());
            }
            catch (FileUploadingFailedException e){
                throw new CustomException(IMAGE_UPLOAD_FAILED);
            }
        }

        // 기본 이미지 URL 찾기
        if(isValidDefaultProfileImageName(request)){
            return defaultImageUrlProvider.getImageUrl(request.defaultProfileImageName());
        }

        // 요청이 잘못되었음
        throw new CustomException(BAD_REQUEST);
    }

    private boolean isValidDefaultProfileImageName(RegisterUserRequest request) {
        return request.defaultProfileImageName() != null && defaultImageUrlProvider.containsKey(request.defaultProfileImageName());
    }

    private boolean isNotEmtpyProfileImage(RegisterUserRequest request) {
        return request.profileImage() != null && !request.profileImage().isEmpty();
    }
}
