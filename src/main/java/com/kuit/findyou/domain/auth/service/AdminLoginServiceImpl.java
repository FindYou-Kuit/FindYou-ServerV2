package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.response.AdminLoginResponse;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class AdminLoginServiceImpl implements AdminLoginService{
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${admin.admin-user-id}")
    private Long adminUserId;

    @Value("${admin.access-ttl-ms}")
    private Long adminAccessTtlMs;

    @Override
    public AdminLoginResponse adminLogin() {
        User user = userRepository.findById(adminUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String accessToken = jwtUtil.createAccessJwt(user.getId(), Role.ADMIN, adminAccessTtlMs);

        return new AdminLoginResponse(user.getId(), accessToken);
    }
}
