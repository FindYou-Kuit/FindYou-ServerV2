package com.kuit.findyou.domain.user.service.register;

import com.kuit.findyou.domain.user.constant.DefaultProfileImage;
import com.kuit.findyou.domain.user.dto.request.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.response.RegisterUserResponse;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegisterUserServiceImpl implements RegisterUserService {
    private final UserRepository userRepository;
    private final ImageUploader imageUploader;
    private final JwtUtil jwtUtil;

    @Override
    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        // 카카오 Id가 중복되는 사용자가 있는지 확인
        if(userRepository.findByKakaoId(request.kakaoId()).isPresent()){
            log.info("[registerUser] user with kakaoId {} alreay exists", request.kakaoId());
            throw new CustomException(ALREADY_REGISTERED_USER);
        }

        // 비회원이었는지 확인한 후에 회원 정보 저장
        String profileImageUrl = getProfileImageUrl(request);

        User user = userRepository.findByDeviceId(request.deviceId())
                .map(existing -> {
                    log.info("[registerUser] user with deviceId {} alreay exists", request.deviceId());
                    existing.upgradeToMember(request.kakaoId(), request.nickname(), profileImageUrl);
                    return existing;
                })
                .orElseGet(()->{
                    log.info("[registerUser] user not found");
                    return mapToUser(request, profileImageUrl);
                });

        User save = userRepository.save(user);

        // 회원가입 완료 응답하기
        String accessToken = jwtUtil.createAccessJwt(save.getId(), save.getRole());
        return new RegisterUserResponse(save.getId(), save.getName(), accessToken);
    }

    private User mapToUser(RegisterUserRequest request, String profileImageUrl) {
        return User.builder()
                .kakaoId(request.kakaoId())
                .name(request.nickname())
                .profileImageUrl(profileImageUrl)
                .role(Role.USER)
                .deviceId(request.deviceId())
                .build();
    }

    private String getProfileImageUrl(RegisterUserRequest request) {
        // 프로필 이미지 설정 관련 검증
        MultipartFile profileImage = request.profileImageFile();
        String defaultProfileImageName = request.defaultProfileImageName();

        if(validateProfileImage(profileImage, defaultProfileImageName)){
            // 요청이 잘못되었음
            throw new CustomException(BAD_REQUEST);
        }

        // 인프라에 이미지 업로드
        if(!isEmptyProfileImageFile(profileImage)){
            try{
                return imageUploader.upload(profileImage);
            }
            catch (FileUploadingFailedException e){
                throw new CustomException(IMAGE_UPLOAD_FAILED);
            }
        }

        // 기본 이미지 이름 반환
        return defaultProfileImageName;
    }

    private boolean validateProfileImage(MultipartFile profileFile, String defaultName) {
        // 둘 다 잘못된 값이거나, 둘 다 올바른 값이면 잘못된 요청으로 간주
        boolean invalidName = isInvalidDefaultProfileImageName(defaultName);
        boolean emptyFile = isEmptyProfileImageFile(profileFile);
        return invalidName && emptyFile || !invalidName && !emptyFile;
    }

    private boolean isInvalidDefaultProfileImageName(String name) {
        return name == null || !DefaultProfileImage.validate(name);
    }

    private boolean isEmptyProfileImageFile(MultipartFile file) {
        return file == null || file.isEmpty();
    }
}
