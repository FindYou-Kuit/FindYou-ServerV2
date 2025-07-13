package com.kuit.findyou.global.jwt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.global.common.response.BaseErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.UNAUTHORIZED;

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
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        log.info("[commence] 인증 실패로 인증 실패 응답 발생 ");

        BaseErrorResponse body = new BaseErrorResponse(UNAUTHORIZED);
        response.setStatus(401);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}