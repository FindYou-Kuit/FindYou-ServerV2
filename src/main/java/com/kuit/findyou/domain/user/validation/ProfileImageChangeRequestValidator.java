package com.kuit.findyou.domain.user.validation;

import com.kuit.findyou.domain.user.constant.DefaultProfileImage;
import com.kuit.findyou.domain.user.dto.request.ChangeProfileImageRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ProfileImageChangeRequestValidator implements ConstraintValidator<ValidProfileImageChangeRequest, ChangeProfileImageRequest> {
    @Override
    public boolean isValid(ChangeProfileImageRequest value, ConstraintValidatorContext context) {
        if (value == null) return true;

        MultipartFile file = value.profileImageFile();
        String defaultName = value.defaultProfileImageName();

        boolean hasFile = (file != null) && !file.isEmpty();
        boolean hasDefault = defaultName != null && !defaultName.isBlank();

        // 둘 다 있거나 둘 다 없는 경우 -> 잘못된 요청임
        if (hasFile == hasDefault) return false;

        return true;
    }
}
