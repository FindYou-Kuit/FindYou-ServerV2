package com.kuit.findyou.domain.home.repository;

import java.util.Optional;

public interface CacheSnapshotRepository {

    Optional<String> find(String cacheKey);

    void insert(String cacheKey, String content);

    void delete(String cacheKey);
}
