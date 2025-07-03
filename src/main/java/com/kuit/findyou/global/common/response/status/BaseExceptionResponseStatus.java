package com.kuit.findyou.global.common.response.status;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BaseExceptionResponseStatus implements ResponseStatus{
    SUCCESS(200, "요청에 성공했습니다."),
    BAD_REQUEST(400, "유효하지 않은 요청입니다."),
    API_NOT_FOUND(404, "존재하지 않는 API입니다."),
    INTERNAL_SERVER_ERROR(50000, "서버 내부 오류입니다.");

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
