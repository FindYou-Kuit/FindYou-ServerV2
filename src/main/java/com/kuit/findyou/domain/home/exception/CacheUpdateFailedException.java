package com.kuit.findyou.domain.home.exception;

public class CacheUpdateFailedException extends Exception {
    public CacheUpdateFailedException() {
        super("홈화면 통계 캐시 업데이트 실패");
    }

    public CacheUpdateFailedException(String message) {
        super(message);
    }

    public CacheUpdateFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
