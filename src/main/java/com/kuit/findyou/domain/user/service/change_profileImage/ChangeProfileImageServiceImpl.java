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
        User user = userRepository.getReferenceById(userId);

        String toSave;
        if (request.profileImageFile() != null && !request.profileImageFile().isEmpty()) {
            try {
                toSave = imageUploader.upload(request.profileImageFile());
            } catch (FileUploadingFailedException e) {
                throw new CustomException(IMAGE_UPLOAD_FAILED);
            }
        } else {
            //enum 이름을 소문자로 저장
            toSave = Arrays.stream(DefaultProfileImage.values())
                    .filter(v -> v.getName().equalsIgnoreCase(request.defaultProfileImageName()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(BAD_REQUEST))
                    .getName();
        }
        user.changeProfileImage(toSave);
    }
}
