package com.kuit.findyou.global.external.exception;

public class VolunteerWorkApiException extends RuntimeException {

    public VolunteerWorkApiException(String message) {
        super(message);
    }

    public VolunteerWorkApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
