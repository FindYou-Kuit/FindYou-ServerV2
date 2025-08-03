package com.kuit.findyou.global.external.exception;

public class OpenAiParsingException extends RuntimeException {
    public OpenAiParsingException(String message) {
        super(message);
    }

    public OpenAiParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}

