package com.kuit.findyou.global.external.exception;

public class OpenAiClientException extends RuntimeException {

    public OpenAiClientException(String message) {
        super(message);
    }

    public OpenAiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
