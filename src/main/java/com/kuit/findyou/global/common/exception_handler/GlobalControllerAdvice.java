package com.kuit.findyou.global.common.exception_handler;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.response.BaseErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;


@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {
    // 잘못된 요청일 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TypeMismatchException.class)
    public BaseErrorResponse handle_TypeMismatchException(TypeMismatchException e){
        log.error("[handle_TypeMismatchException]", e);
        return new BaseErrorResponse(BAD_REQUEST);
    }

    // 요청한 api가 없을 경우
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public BaseErrorResponse handle_NoHandlerFoundException(NoHandlerFoundException e){
        log.error("[handle_NoHandlerFoundException]", e);
        return new BaseErrorResponse(API_NOT_FOUND);
    }

    // 런타임 오류가 발생한 경우
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public BaseErrorResponse handle_RuntimeException(RuntimeException e) {
        log.error("[handle_RuntimeException]", e);
        return new BaseErrorResponse(INTERNAL_SERVER_ERROR);
    }

    // 요청에 필요한 인자가 없는 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse handle_MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("[handle_MethodArgumentNotValidException]", e);
        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return new BaseErrorResponse(BAD_REQUEST, defaultMessage);
    }

    // 커스텀 예외의 경우
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CustomException.class)
    public BaseErrorResponse handle_CustomException(CustomException e) {
        log.error("[handle_CustomException]", e);
        return new BaseErrorResponse(e.getExceptionStatus());
    }
}
