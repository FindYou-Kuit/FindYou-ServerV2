package com.kuit.findyou.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RedisRefreshTokenRepositoryImpl implements RedisRefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${findyou.jwt.expiration-ms.refresh-token}")
    private Long refreshTokenExpireMs;
    @Value("${findyou.jwt.refresh-token-redis-key-prefix}")
    private String refreshTokenKeyPrefix;

    private String key(Long userId) {
        return refreshTokenKeyPrefix + userId;
    }

    @Override
    public Optional<String> findByUserId(Long userId) {
        String value = redisTemplate.opsForValue().get(key(userId));
        return Optional.ofNullable(value);
    }

    @Override
    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(key(userId), refreshToken, Duration.ofMillis(refreshTokenExpireMs));
    }
}
