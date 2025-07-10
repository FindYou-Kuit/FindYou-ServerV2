package com.kuit.findyou.global.common.swagger;

import com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Getter
public enum SwaggerResponseDescription {

    TEST(new LinkedHashSet<>(Set.of(
            TEST_EXCEPTION
    ))),

    PROTECTING_REPORT_DETAIL(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            PROTECTING_REPORT_NOT_FOUND,
            ILLEGAL_TAG
    ))),

    MISSING_REPORT_DETAIL(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            MISSING_REPORT_NOT_FOUND,
            ILLEGAL_TAG
    ))),

    WITNESS_REPORT_DETAIL(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            WITNESS_REPORT_NOT_FOUND,
            ILLEGAL_TAG
    )));

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
