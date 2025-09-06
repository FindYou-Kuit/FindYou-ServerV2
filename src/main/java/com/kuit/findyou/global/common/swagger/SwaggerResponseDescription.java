package com.kuit.findyou.global.common.swagger;

import com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Getter
public enum SwaggerResponseDescription {
    ADD_INTEREST_ANIMAL(new LinkedHashSet<>(Set.of(
            DUPLICATE_INTEREST_REPORT,
            REPORT_NOT_FOUND
    ))),

    GET_HOME(new LinkedHashSet<>(Set.of(
            HOME_STATISTICS_UPDATE_FAILED
    ))),

    REGISTER_USER(new LinkedHashSet<>(Set.of(
            ALREADY_REGISTERED_USER,
            IMAGE_UPLOAD_FAILED
    ))),

    CHECK_DUPLICATE_NICKNAME(new LinkedHashSet<>(Set.of())),

    GUEST_LOGIN(new LinkedHashSet<>(Set.of(
            GUEST_LOGIN_FAILED
    ))),

    KAKAO_LOGIN(new LinkedHashSet<>(Set.of())),

    TEST(new LinkedHashSet<>(Set.of(
            TEST_EXCEPTION
    ))),

    PROTECTING_REPORT_DETAIL(new LinkedHashSet<>(Set.of(
            PROTECTING_REPORT_NOT_FOUND
    ))),

    MISSING_REPORT_DETAIL(new LinkedHashSet<>(Set.of(
            MISSING_REPORT_NOT_FOUND
    ))),

    WITNESS_REPORT_DETAIL(new LinkedHashSet<>(Set.of(
            WITNESS_REPORT_NOT_FOUND
    ))),

    BREED_AI_DETECTION(new LinkedHashSet<>(Set.of(
            BREED_ANALYSIS_FAILED
    ))),

    RECOMMENDED_VIDEO(new LinkedHashSet<>(Set.of(
            RECOMMENDED_VIDEO_NOT_FOUND
    ))),

    RECOMMENDED_NEWS(new LinkedHashSet<>(Set.of(
            RECOMMENDED_NEWS_NOT_FOUND
    ))),

    GET_SIGUNGU_LIST(new LinkedHashSet<>(Set.of(
            SIDO_NOT_FOUND
    ))),

    IMAGE_UPLOAD(new LinkedHashSet<>(Set.of(
            IMAGE_UPLOAD_LIMIT_EXCEEDED,
            INVALID_IMAGE_FORMAT,
            IMAGE_UPLOAD_FAILED,
            IMAGE_SIZE_EXCEEDED
    ))),

    CHANGE_PROFILE_IMAGE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            IMAGE_UPLOAD_FAILED,
            IMAGE_SIZE_EXCEEDED
    ))),

    DELETE_REPORT(new LinkedHashSet<>(Set.of(
            MISMATCH_REPORT_USER,
            REPORT_NOT_FOUND
    ))),

    DEFAULT(new LinkedHashSet<>());



    private final Set<BaseExceptionResponseStatus> exceptionResponseStatusSet;

    SwaggerResponseDescription(Set<BaseExceptionResponseStatus> exceptionResponseStatusSet) {
        exceptionResponseStatusSet.addAll(new LinkedHashSet<>(Set.of(
                BAD_REQUEST,
                UNAUTHORIZED,
                FORBIDDEN,
                API_NOT_FOUND,
                METHOD_NOT_ALLOWED,
                INTERNAL_SERVER_ERROR
        )));

        this.exceptionResponseStatusSet = exceptionResponseStatusSet;
    }
}
