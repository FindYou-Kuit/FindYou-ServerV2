package com.kuit.findyou.global.external.exception;

import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;

public class MissingAnimalApiClientException extends RuntimeException {

    public MissingAnimalApiClientException(String message) {
        super(message);
    }

    public MissingAnimalApiClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingAnimalApiClientException(ExternalExceptionMessage externalExceptionMessage) {
        super(externalExceptionMessage.getValue());
    }

    public MissingAnimalApiClientException(ExternalExceptionMessage externalExceptionMessage, Throwable cause) {
        super(externalExceptionMessage.getValue(), cause);
    }
}