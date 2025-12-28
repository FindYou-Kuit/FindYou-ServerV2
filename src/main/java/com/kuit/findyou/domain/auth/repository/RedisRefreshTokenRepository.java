package com.kuit.findyou.domain.auth.repository;

import java.util.Optional;

public interface RedisRefreshTokenRepository {
    Optional<String> findByUserId(Long userId);

    void save(Long id, String refreshToken);
}
