package com.kuit.findyou.global.jwt.exception;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.response.status.ResponseStatus;

public class InvalidJwtException extends CustomException {
    public InvalidJwtException(ResponseStatus exceptionStatus){
        super(exceptionStatus);
    }
}
