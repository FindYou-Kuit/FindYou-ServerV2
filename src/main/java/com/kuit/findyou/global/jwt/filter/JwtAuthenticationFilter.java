package com.kuit.findyou.global.jwt.filter;

import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.jwt.security.CustomUserDetails;
import com.kuit.findyou.global.jwt.security.CustomUserDetailsService;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if(token != null || jwtUtil.validateJwt(token)){
            // UserDetails 생성
            Long userId = jwtUtil.getUserId(token);

            // userDetails 조회
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(String.valueOf(userId));

            //스프링 시큐리티 인증 객체를 생성하고 컨텍스트에 저장
            Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // token을 argument resolver에 전달
            request.setAttribute("token", token);
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if(authorization != null && authorization.startsWith("Bearer ")){
            String token = authorization.split(" ")[1];
            return token;
        }
        log.info("token does not exist!");
        return null;
    }
}
