package com.kuit.findyou.domain.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProfileImageChangeRequestValidator.class)
public @interface ValidProfileImageChangeRequest {
    String message() default "잘못된 프로필 이미지 변경 요청입니다. 프로필 이미지와 기본 이미지 중 하나만 요청해야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
