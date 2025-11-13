package com.kuit.findyou.domain.user.service.change_profileImage;

import com.kuit.findyou.domain.user.constant.DefaultProfileImage;
import com.kuit.findyou.domain.user.dto.request.ChangeProfileImageRequest;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
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
        try {
            User user = userRepository.getReferenceById(userId);

            String toSave;
            if (request.profileImageFile() != null) {
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
            String oldImageUrl = user.getProfileImageUrl();
            user.changeProfileImage(toSave);
            if (isUploadedFile(oldImageUrl)) {
                String imageKey = extractImageKeyFromUrl(oldImageUrl);
                imageUploader.delete(imageKey);
            }
        }catch (EntityNotFoundException e) {
            throw new CustomException(USER_NOT_FOUND);
        }
    }

    private boolean isUploadedFile(String url) {
        if (url == null) return false;
        return Arrays.stream(DefaultProfileImage.values())
                .noneMatch(defaultImage -> defaultImage.getName().equalsIgnoreCase(url));
    }

    private String extractImageKeyFromUrl(String url) {
        try {
            return new URI(url).getPath().substring(1);
        } catch (URISyntaxException e) {
            throw new CustomException(BAD_REQUEST);
        }
    }
}
