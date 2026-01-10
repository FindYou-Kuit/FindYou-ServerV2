package com.kuit.findyou.global.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminAllowlistFilter extends OncePerRequestFilter {

    private final AntPathMatcher matcher = new AntPathMatcher();

    // ADMIN에게 허용되는 API
    private static final List<Allow> ADMIN_ALLOWLIST = List.of(
            new Allow(HttpMethod.GET.name(), "/api/v2/reports/protecting-reports/random-s3"),
            new Allow(HttpMethod.GET.name(), "/api/v2/reports/missing-reports/random-s3"),
            new Allow(HttpMethod.POST.name(), "/api/v2/images/upload")
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                String method = request.getMethod();
                String path = request.getRequestURI();

                boolean allowed = ADMIN_ALLOWLIST.stream()
                        .anyMatch(a -> a.method.equals(method) && matcher.match(a.pathPattern, path));

                if (!allowed) {
                    throw new AccessDeniedException("ADMIN은 허용된 API만 호출할 수 있습니다.");
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private static class Allow {
        final String method;
        final String pathPattern;

        private Allow(String method, String pathPattern) {
            this.method = method;
            this.pathPattern = pathPattern;
        }
    }
}
