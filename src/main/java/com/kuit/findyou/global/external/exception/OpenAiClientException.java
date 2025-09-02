package com.kuit.findyou.global.external.exception;

import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;

public class OpenAiClientException extends RuntimeException {

    public OpenAiClientException(String message) {
        super(message);
    }

    public OpenAiClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenAiClientException(ExternalExceptionMessage externalExceptionMessage) {
        super(externalExceptionMessage.getValue());
    }

    public OpenAiClientException(ExternalExceptionMessage externalExceptionMessage, Throwable cause) {
        super(externalExceptionMessage.getValue(), cause);
    }
}
