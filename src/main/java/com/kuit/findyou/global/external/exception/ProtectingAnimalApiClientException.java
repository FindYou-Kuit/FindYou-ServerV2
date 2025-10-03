package com.kuit.findyou.global.external.exception;

import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;

public class ProtectingAnimalApiClientException extends RuntimeException {

    public ProtectingAnimalApiClientException(String message) {
        super(message);
    }

    public ProtectingAnimalApiClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtectingAnimalApiClientException(ExternalExceptionMessage externalExceptionMessage) {
        super(externalExceptionMessage.getValue());
    }

    public ProtectingAnimalApiClientException(ExternalExceptionMessage externalExceptionMessage, Throwable cause) {
        super(externalExceptionMessage.getValue(), cause);
    }
}