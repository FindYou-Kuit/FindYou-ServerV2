package com.kuit.findyou.global.jwt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.global.common.response.BaseErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.FORBIDDEN;


@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        BaseErrorResponse body =  new BaseErrorResponse(FORBIDDEN);
        String json = objectMapper.writeValueAsString(body);

        response.setStatus(FORBIDDEN.getCode());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(json);
    }
}