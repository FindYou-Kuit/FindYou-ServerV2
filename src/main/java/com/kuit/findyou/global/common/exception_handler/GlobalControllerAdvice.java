package com.kuit.findyou.global.common.exception_handler;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.response.BaseErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;


@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {
    // 잘못된 요청일 경우
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<BaseErrorResponse> handle_TypeMismatchException(TypeMismatchException e){
        log.error("[handle_TypeMismatchException]", e);
        return ResponseEntity
                .badRequest()
                .body(new BaseErrorResponse(BAD_REQUEST));
    }

    // 요청한 api가 없을 경우
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseErrorResponse> handle_NoHandlerFoundException(NoHandlerFoundException e){
        log.error("[handle_NoHandlerFoundException]", e);
        return ResponseEntity
                .badRequest()
                .body(new BaseErrorResponse(API_NOT_FOUND));
    }

    // 런타임 오류가 발생한 경우
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseErrorResponse> handle_RuntimeException(RuntimeException e) {
        log.error("[handle_RuntimeException]", e);
        return ResponseEntity
                .internalServerError()
                .body(new BaseErrorResponse(INTERNAL_SERVER_ERROR));
    }

    // 요청에 필요한 인자가 없는 경우
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handle_MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("[handle_MethodArgumentNotValidException]", e);
        FieldError fieldError = e.getBindingResult().getFieldError(); // NPE 가능성으로 인해 검증
        String defaultMessage = (fieldError != null) ? e.getBindingResult().getFieldError().getDefaultMessage() : "Invalid request";
        return ResponseEntity
                .badRequest()
                .body(new BaseErrorResponse(BAD_REQUEST, defaultMessage));
    }

    // 커스텀 예외의 경우
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseErrorResponse> handle_CustomException(CustomException e) {
        log.error("[handle_CustomException]", e);
        return ResponseEntity
                .status(HttpStatusCode.valueOf(e.getExceptionStatus().getCode()))
                .body(new BaseErrorResponse(e.getExceptionStatus()));
    }
}
