package com.kuit.findyou.global.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kuit.findyou.domain.home.repository.CacheSnapshotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheSnapshotServiceTest {
    @InjectMocks
    private CacheSnapshotService service;

    @Mock
    private CacheSnapshotRepository cacheSnapshotRepository;

    private String REDIS_CACHE_KEY = "home:statistics";


    @DisplayName("스냅샷이 존재하면 삭제후에 추가된다")
    @Test
    void should_InsertAfterDeletion_When_SnapshotExists() throws JsonProcessingException {
        // given
        String json = "{}";
        when(cacheSnapshotRepository.find(eq(REDIS_CACHE_KEY))).thenReturn(Optional.of("{}"));

        // when
        service.saveJsonCache(REDIS_CACHE_KEY, json);

        // then
        verify(cacheSnapshotRepository).delete(eq(REDIS_CACHE_KEY));
        verify(cacheSnapshotRepository).insert(eq(REDIS_CACHE_KEY), eq(json));
    }

    @DisplayName("스냅샷이 없으면 추가만 수행한다")
    @Test
    void should_OnlyInsert_When_NoSnapshotExists() throws JsonProcessingException {
        // given
        String json = "{}";
        when(cacheSnapshotRepository.find(eq(REDIS_CACHE_KEY))).thenReturn(Optional.empty());

        // when
        service.saveJsonCache(REDIS_CACHE_KEY, json);

        // then
        verify(cacheSnapshotRepository).insert(eq(REDIS_CACHE_KEY), eq(json));
        verify(cacheSnapshotRepository, never()).delete(any());
    }

    @DisplayName("스냅샷이 없으면 비어있는 결과를 반환한다.")
    @Test
    void should_ResultIsEmpty_When_NoSnapShotExists(){
        // when
        when(cacheSnapshotRepository.find(eq(REDIS_CACHE_KEY))).thenReturn(Optional.empty());

        // given
        Optional<String> result = service.findJsonCache(REDIS_CACHE_KEY);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("스냅샷이 존재하면 결과가 존재한다.")
    @Test
    void should_ResultIsPresent_When_SnapShotExists() throws JsonProcessingException {
        // when
        String json = "{}";
        when(cacheSnapshotRepository.find(eq(REDIS_CACHE_KEY))).thenReturn(Optional.of(json));

        // given
        Optional<String> result = service.findJsonCache(REDIS_CACHE_KEY);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(json);
    }

}