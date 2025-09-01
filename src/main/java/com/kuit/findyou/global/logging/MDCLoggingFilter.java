package com.kuit.findyou.global.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class MDCLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String rawRequestId = request.getHeader("X-Request-ID");
            String requestId = (rawRequestId == null || rawRequestId.trim().isEmpty())
                    ? UUID.randomUUID().toString()
                    : rawRequestId;

            MDC.put("request_id", requestId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}