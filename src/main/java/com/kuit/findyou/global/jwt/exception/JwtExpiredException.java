package com.kuit.findyou.global.jwt.exception;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.response.status.ResponseStatus;

public class JwtExpiredException extends CustomException {
    public JwtExpiredException(ResponseStatus exceptionStatus){
        super(exceptionStatus);
    }
}
