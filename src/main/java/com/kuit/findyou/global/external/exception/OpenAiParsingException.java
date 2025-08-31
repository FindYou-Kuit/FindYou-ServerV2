package com.kuit.findyou.global.external.exception;

import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;

public class OpenAiParsingException extends RuntimeException {
    public OpenAiParsingException(String message) {
        super(message);
    }

    public OpenAiParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenAiParsingException(ExternalExceptionMessage externalExceptionMessage) {
        super(externalExceptionMessage.getValue());
    }

    public OpenAiParsingException(ExternalExceptionMessage externalExceptionMessage, Throwable cause) {
        super(externalExceptionMessage.getValue(), cause);
    }
}

