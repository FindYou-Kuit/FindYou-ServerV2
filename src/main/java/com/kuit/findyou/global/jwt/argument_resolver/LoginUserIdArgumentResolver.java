package com.kuit.findyou.global.jwt.argument_resolver;

import com.kuit.findyou.global.jwt.annotation.LoginUserId;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.kuit.findyou.global.jwt.constant.JwtAutenticationFilterConstants.TOKEN_FOR_ARGUMENT_RESOLVER;

@Slf4j
@RequiredArgsConstructor
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtUtil jwtUtil;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Long.class) && parameter.hasParameterAnnotation(LoginUserId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = (String) request.getAttribute(TOKEN_FOR_ARGUMENT_RESOLVER.getValue());
        if(token == null) {
            return 1L;
        }
        Long userId = jwtUtil.getUserId(token);
        log.info("userId = {}",  userId);
        return userId;
    }
}
