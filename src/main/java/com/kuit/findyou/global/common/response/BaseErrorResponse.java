package com.kuit.findyou.global.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.kuit.findyou.global.common.response.status.ResponseStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({"success", "code", "message", "timestamp"})
public class BaseErrorResponse implements ResponseStatus {
    private final boolean success;
    private final int code;
    private final String message;
    private final LocalDateTime timestamp;

    public BaseErrorResponse(ResponseStatus status) {
        this.success = false;
        this.code = status.getCode();
        this.message = status.getMessage();
        this.timestamp = LocalDateTime.now();
    }

    public BaseErrorResponse(ResponseStatus status, String message) {
        this.success = false;
        this.code = status.getCode();
        this.message = message;
        this.timestamp = LocalDateTime.now();
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
