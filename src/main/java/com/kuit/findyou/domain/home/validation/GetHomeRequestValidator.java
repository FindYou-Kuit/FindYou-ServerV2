package com.kuit.findyou.domain.home.validation;

import com.kuit.findyou.domain.home.dto.GetHomeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class GetHomeRequestValidator implements ConstraintValidator<ValidGetHomeRequest,GetHomeRequest> {

    @Override
    public boolean isValid(GetHomeRequest getHomeRequest, ConstraintValidatorContext constraintValidatorContext) {
        Double lat = getHomeRequest.lat();
        Double lng = getHomeRequest.lng();

        // 둘 중 하나만 있으면 잘못된 요청
        if((lat == null && lng != null) || (lat != null && lng == null)){
            return false;
        }

        // 둘 다 없음
        if(lat == null) return true;

        // 값 범위 검증
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }
}
