package com.kuit.findyou.global.common.service;

import com.kuit.findyou.domain.home.repository.CacheSnapshotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheSnapshotService {
    private final CacheSnapshotRepository cacheSnapshotRepository;

    @Transactional
    public void saveJsonCache(String cacheKey, String jsonCache) {
        var cache = cacheSnapshotRepository.find(cacheKey);
            if(cache.isPresent()){
                cacheSnapshotRepository.delete(cacheKey);
            }
            cacheSnapshotRepository.insert(cacheKey, jsonCache);
    }

    public Optional<String> findJsonCache(String cacheKey) {
            return cacheSnapshotRepository.find(cacheKey);
    }
}