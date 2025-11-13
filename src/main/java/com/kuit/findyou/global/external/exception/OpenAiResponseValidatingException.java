package com.kuit.findyou.global.external.exception;

import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;

public class OpenAiResponseValidatingException extends RuntimeException {
    public OpenAiResponseValidatingException(String message) {
        super(message);
    }

    public OpenAiResponseValidatingException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenAiResponseValidatingException(ExternalExceptionMessage externalExceptionMessage) {
        super(externalExceptionMessage.getValue());
    }

    public OpenAiResponseValidatingException(ExternalExceptionMessage externalExceptionMessage, Throwable cause) {
        super(externalExceptionMessage.getValue(), cause);
    }
}

