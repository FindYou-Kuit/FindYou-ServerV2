package com.kuit.findyou.global.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            String requestId = ((HttpServletRequest)servletRequest).getHeader("X-Request-ID");
            MDC.put("request_id", Objects.toString(requestId, UUID.randomUUID().toString()));

            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear(); // 메모리 누수 방지
        }
    }
}