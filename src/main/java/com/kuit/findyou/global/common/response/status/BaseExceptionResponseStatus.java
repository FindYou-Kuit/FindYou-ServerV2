package com.kuit.findyou.global.common.response.status;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum BaseExceptionResponseStatus implements ResponseStatus{
    // 테스트
    TEST_EXCEPTION(100, "테스트용 예외입니다."),

    // 성공
    SUCCESS(200, "요청에 성공했습니다."),

    // 공통 에러
    BAD_REQUEST(400, "유효하지 않은 요청입니다."),
    UNAUTHORIZED(401, "인증 자격이 없습니다."),
    FORBIDDEN(403, "권한이 없습니다."),
    API_NOT_FOUND(404, "존재하지 않는 API입니다."),
    METHOD_NOT_ALLOWED(405, "유효하지 않은 Http 메서드입니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류입니다."),

    // JWT 토큰
    INVALID_JWT(401, "올바르지 않은 토큰입니다."),
    EXPIRED_JWT(401, "만료된 토큰입니다"),
    JWT_NOT_FOUND(400, "토큰을 찾을 수 없습니다"),

    // 유저 - User
    USER_NOT_FOUND(404, "존재하지 않는 유저입니다."),

    // 글 - Report
    PROTECTING_REPORT_NOT_FOUND(404, "존재하지 않는 보호글입니다."),
    MISSING_REPORT_NOT_FOUND(404, "존재하지 않는 실종 신고글입니다."),
    WITNESS_REPORT_NOT_FOUND(404, "존재하지 않는 목격 신고글입니다."),
    ILLEGAL_TAG(500, "잘못된 태그값입니다."),

    // 추천 컨텐츠 - Recommendation
    RECOMMENDED_VIDEO_NOT_FOUND(404, "추천 영상이 존재하지 않습니다."),
    RECOMMENDED_NEWS_NOT_FOUND(404, "추천 기사가 존재하지 않습니다.");

    private final boolean success = false;
    private final int code;
    private final String message;

    @Override
    public boolean getSuccess() { return this.success; }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
