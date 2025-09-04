package com.kuit.findyou.domain.user.service.change_profileImage;

import com.kuit.findyou.domain.user.dto.request.ChangeProfileImageRequest;

public interface ChangeProfileImageService {
    void changeProfileImage(Long userId, ChangeProfileImageRequest request);
}
