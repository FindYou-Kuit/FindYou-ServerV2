package com.kuit.findyou.domain.home.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = GetHomeRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGetHomeRequest {
    String message() default "Invalid coordinate values";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
