package com.kuit.findyou.global.config;

import com.kuit.findyou.global.jwt.security.CustomAuthenticationEntryPoint;
import com.kuit.findyou.global.jwt.filter.JwtAuthenticationFilter;
import com.kuit.findyou.global.logging.MDCLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MDCLoggingFilter mdcLoggingFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final String[] PERMIT_URL = {
            "/api/v2/auth/**", "/swagger-ui/**", "/api-docs", "/swagger-ui-custom.html",
            "/v3/api-docs/**", "/api-docs/**", "/swagger-ui.html", "/swagger-ui/index.html",
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http
                .csrf((auth) -> auth.disable());

        http
                .formLogin((auth)->auth.disable());

        http
                .httpBasic((auth)->auth.disable());

        // todo 인증 비활성화
        // 토큰 기반 인증 비활성화
        http
                .authorizeHttpRequests((auth)-> auth
                        .anyRequest().permitAll());

        // 토큰 기반 인증 활성화
//        http
//                .authorizeHttpRequests((auth)-> auth
//                        .requestMatchers(PERMIT_URL).permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/v2/users").permitAll()
//                        .anyRequest().authenticated());

        // MDC 필터 추가
        http
                .addFilterBefore(mdcLoggingFilter, UsernamePasswordAuthenticationFilter.class);

        // 토큰 검증 필터 추가
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 토큰 검증 예외 처리 추가
        http
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(customAuthenticationEntryPoint));

        http
                .sessionManagement((session)->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
