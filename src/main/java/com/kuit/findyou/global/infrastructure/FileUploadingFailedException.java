package com.kuit.findyou.global.infrastructure;

public class FileUploadingFailedException extends RuntimeException {
    public FileUploadingFailedException(String message) {
        super(message);
    }

    public FileUploadingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
