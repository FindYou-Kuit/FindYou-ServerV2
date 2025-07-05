package com.kuit.findyou.global.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.kuit.findyou.global.common.response.status.ResponseStatus;
import lombok.Getter;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.SUCCESS;


@Getter
@JsonPropertyOrder({"success", "code", "message", "data"})
public class BaseResponse<T> implements ResponseStatus {
    private final boolean success;
    private final int code;
    private final String message;
    private final T data;

    public BaseResponse(T data) {
        this.success = true;
        this.code = SUCCESS.getCode();
        this.message = SUCCESS.getMessage();
        this.data = data;
    }

    @Override
    public boolean getSuccess() {
        return this.success;
    }
    @Override
    public int getCode() {
        return this.code;
    }
    @Override
    public String getMessage() {
        return this.message;
    }
}
