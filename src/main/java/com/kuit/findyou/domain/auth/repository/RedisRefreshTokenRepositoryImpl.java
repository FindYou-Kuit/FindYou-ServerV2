package com.kuit.findyou.domain.auth.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RedisRefreshTokenRepositoryImpl implements RedisRefreshTokenRepository {

    @Override
    public Optional<String> findByUserId(Long userId) {

        return Optional.empty();
    }
}
