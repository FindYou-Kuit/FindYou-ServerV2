package com.kuit.findyou.global.jwt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.global.common.response.BaseErrorResponse;
import com.kuit.findyou.global.common.response.status.ResponseStatus;
import com.kuit.findyou.global.jwt.constant.JwtErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;
import static com.kuit.findyou.global.jwt.constant.JwtAutenticationFilterConstants.JWT_ERROR_CODE;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final HandlerExceptionResolver resolver;
    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver, ObjectMapper objectMapper){
        this.resolver = resolver;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException{
        log.info("[commence] 인증 실패로 인증 실패 응답 발생 ");

        JwtErrorCode errorCode = (JwtErrorCode) request.getAttribute(JWT_ERROR_CODE.getValue());

        if(errorCode != null){
            // JWT 예외 원인에 따라서 응답 세분화
            log.info("[commence] Jwt 관련 Exception 발생! errorCode={}", errorCode);
            switch (errorCode){
                case INVALID_JWT_ERROR -> writeErrorResponse(response, INVALID_JWT);
                case EXPIRED_JWT_ERROR -> writeErrorResponse(response, EXPIRED_JWT);
                case JWT_NOT_FOUND_ERROR -> writeErrorResponse(response, JWT_NOT_FOUND);
                default ->  writeErrorResponse(response, UNAUTHORIZED);
            }
        }
        else{
            writeErrorResponse(response, UNAUTHORIZED);
        }
    }

    public void writeErrorResponse(HttpServletResponse response, ResponseStatus status) throws IOException{
        BaseErrorResponse body =  new BaseErrorResponse(status);
        String json = objectMapper.writeValueAsString(body);
//        log.info("[commence] 응답 JSON = {}", json);
        response.setStatus(status.getCode());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(json);
    }
}