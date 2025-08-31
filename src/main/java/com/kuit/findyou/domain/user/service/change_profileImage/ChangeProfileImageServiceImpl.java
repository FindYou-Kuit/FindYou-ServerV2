package com.kuit.findyou.domain.user.service.change_profileImage;

import com.kuit.findyou.domain.user.constant.DefaultProfileImage;
import com.kuit.findyou.domain.user.dto.request.ChangeProfileImageRequest;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeProfileImageServiceImpl implements ChangeProfileImageService {
    private final UserRepository userRepository;
    private final ImageUploader imageUploader;

    @Override
    @Transactional
    public void changeProfileImage(Long userId, ChangeProfileImageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        MultipartFile file = request.profileImageFile();
        String defaultName = request.defaultProfileImageName();

        //둘 다 null이거나, 둘 다 채워졌으면 잘못된 요청
        boolean emptyFile = file == null || file.isEmpty();
        boolean invalidDefault = (defaultName == null) || !DefaultProfileImage.validate(defaultName);

        if ((emptyFile && invalidDefault) || (!emptyFile && !invalidDefault)) {
            throw new CustomException(BAD_REQUEST);
        }

        String toSave;
        if (!emptyFile) {
            try {
                toSave = imageUploader.upload(file);  //업로드 후 CDN URL 반환
            } catch (FileUploadingFailedException e) {
                throw new CustomException(IMAGE_UPLOAD_FAILED);
            }
        }
        //기본 프로필 (enum 이름을 소문자로 저장)
        else {
            toSave = normalizeDefaultName(defaultName);
        }

        user.changeProfileImage(toSave);
    }

    private String normalizeDefaultName(String name) {
        return Arrays.stream(DefaultProfileImage.values())
                .filter(v -> v.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(DefaultProfileImage::getName) // 저장은 항상 소문자(name 필드)
                .orElseThrow(() -> new CustomException(BAD_REQUEST));
    }
}
