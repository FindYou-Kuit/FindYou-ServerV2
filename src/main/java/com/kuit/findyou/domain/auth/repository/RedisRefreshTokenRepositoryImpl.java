package com.kuit.findyou.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RedisRefreshTokenRepositoryImpl implements RedisRefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    private String key(Long userId) {
        return "refresh-token:" + userId;
    }

    @Override
    public Optional<String> findByUserId(Long userId) {
        String value = redisTemplate.opsForValue().get(key(userId));
        return Optional.ofNullable(value);
    }
}
