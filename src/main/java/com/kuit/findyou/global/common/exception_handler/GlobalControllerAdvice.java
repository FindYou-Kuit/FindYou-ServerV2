package com.kuit.findyou.global.common.exception_handler;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.response.BaseErrorResponse;
import com.kuit.findyou.global.common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;


@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    /**
     * 숫자/boolean 등의 타입이 일치하지 않는 경우 발생하는 Hibernate 예외 처리
     * 주로 잘못된 파라미터 변환 시 발생 (ex. boolean 필드에 문자열 전달)
     */
    @ExceptionHandler(TypeMismatchException.class)
    public BaseErrorResponse handle_TypeMismatchException(TypeMismatchException e){
        log.error("[handle_TypeMismatchException]", e);
        return new BaseErrorResponse(BAD_REQUEST);
    }

    /**
     * 존재하지 않는 API 경로로 요청했을 때 발생 (404 Not Found)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public BaseErrorResponse handle_NoHandlerFoundException(NoHandlerFoundException e){
        log.error("[handle_NoHandlerFoundException]", e);
        return new BaseErrorResponse(API_NOT_FOUND);
    }

    /**
     * 처리되지 않은 런타임 예외를 전역적으로 처리
     * 서버 내부 오류(500 Internal Server Error) 응답 반환
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseErrorResponse handle_RuntimeException(RuntimeException e) {
        log.error("[handle_RuntimeException]", e);
        return new BaseErrorResponse(INTERNAL_SERVER_ERROR);
    }

    /**
     * @Valid 또는 @Validated를 통해 DTO 필드 유효성 검사에 실패한 경우 처리
     * (e.g., 필수값 누락, 길이 초과 등)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse handle_MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("[handle_MethodArgumentNotValidException]", e);
        FieldError fieldError = e.getBindingResult().getFieldError(); // NPE 가능성으로 인해 검증
        String defaultMessage = (fieldError != null) ? e.getBindingResult().getFieldError().getDefaultMessage() : "Invalid request";
        return new BaseErrorResponse(BAD_REQUEST, defaultMessage);
    }

    /**
     * @RequestParam 필드가 아예 전달되지 않은 경우 처리
     * (e.g., 필수 쿼리 파라미터 누락)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public BaseErrorResponse handle_MissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("[handle_MissingServletRequestParameterException]", e);
        String message = String.format("필수 요청 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        return new BaseErrorResponse(BAD_REQUEST, message);
    }

    /**
     * @RequestParam, @PathVariable 등에서 enum/숫자 등의 타입 변환이 실패한 경우 처리
     * (e.g., 잘못된 enum 값, 숫자 자리에 문자열 입력 등)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseErrorResponse handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("[handleMethodArgumentTypeMismatch]", e);

        String name = e.getName();  // 파라미터 이름
        String value = e.getValue() != null ? e.getValue().toString() : "null";
        String message = String.format("잘못된 '%s' 파라미터 값입니다. 입력값: '%s'", name, value);

        return new BaseErrorResponse(BAD_REQUEST, message);
    }

    /**
     * 프로젝트 내부에서 발생한 CustomException 처리
     * 각 예외가 담고 있는 커스텀 응답 코드와 메시지를 그대로 반환
     */
    @ExceptionHandler(CustomException.class)
    public BaseErrorResponse handle_CustomException(CustomException e) {
        log.error("[handle_CustomException]", e);
        return new BaseErrorResponse(e.getExceptionStatus());
    }
}
