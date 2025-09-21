package com.kuit.findyou.domain.home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.repository.CacheSnapshotRepositoryImpl;
import com.kuit.findyou.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeStatsCacheSnapshotServiceTest {
    @InjectMocks
    private HomeStatsCacheSnapshotService service;

    @Mock
    private CacheSnapshotRepositoryImpl cacheSnapshotRepository;

    @Mock
    private ObjectMapper objectMapper;

    private final String REDIS_CACHE_KEY = "home:statistics";

    @DisplayName("스냅샷이 존재하면 삭제후에 추가된다")
    @Test
    void should_InsertAfterDeletion_When_SnapshotExists() throws JsonProcessingException {
        // given
        GetHomeResponse.TotalStatistics stats = mock(GetHomeResponse.TotalStatistics.class);
        String json = "{}";
        when(objectMapper.writeValueAsString(eq(stats))).thenReturn(json);
        when(cacheSnapshotRepository.find(eq(REDIS_CACHE_KEY))).thenReturn(Optional.of("{}"));

        // when
        service.save(stats);

        // then
        verify(cacheSnapshotRepository).delete(eq(REDIS_CACHE_KEY));
        verify(cacheSnapshotRepository).insert(eq(REDIS_CACHE_KEY), eq(json));
    }

    @DisplayName("스냅샷이 존재하면 바로 추가한다")
    @Test
    void should_OnlyInsert_When_NoSnapshotExists() throws JsonProcessingException {
        // given
        GetHomeResponse.TotalStatistics stats = mock(GetHomeResponse.TotalStatistics.class);
        String json = "{}";
        when(objectMapper.writeValueAsString(eq(stats))).thenReturn(json);
        when(cacheSnapshotRepository.find(eq(REDIS_CACHE_KEY))).thenReturn(Optional.empty());

        // when
        service.save(stats);

        // then
        verify(cacheSnapshotRepository).insert(eq(REDIS_CACHE_KEY), eq(json));
    }

    @DisplayName("스냅샷이 없으면 비어있는 결과를 반환한다.")
    @Test
    void should_ResultIsEmpty_When_NoSnapShotExists(){
        // when
        when(cacheSnapshotRepository.find(eq(REDIS_CACHE_KEY))).thenReturn(Optional.empty());

        // given
        Optional<GetHomeResponse.TotalStatistics> result = service.find();

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("스냅샷이 존재하면 결과가 존재한다.")
    @Test
    void should_ResultIsPresent_When_SnapShotExists() throws JsonProcessingException {
        // when
        String json = "{}";
        when(cacheSnapshotRepository.find(eq(REDIS_CACHE_KEY))).thenReturn(Optional.of(json));
        GetHomeResponse.TotalStatistics mockStats = mock(GetHomeResponse.TotalStatistics.class);
        when(objectMapper.readValue(eq(json), eq(GetHomeResponse.TotalStatistics.class))).thenReturn(mockStats);

        // given
        Optional<GetHomeResponse.TotalStatistics> result = service.find();

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mockStats);
    }

    @DisplayName("json문자열을 역직렬화하는 도중에 예외가 발생하면 커스텀 예외를 발생시킨다")
    @Test
    void should_ThrowCustomException_When_FindThrowsJsonProcessingException() throws Exception{
        // given
        String json = "{}";
        when(cacheSnapshotRepository.find(eq(REDIS_CACHE_KEY))).thenReturn(Optional.of(json));
        when(objectMapper.readValue(eq(json), eq(GetHomeResponse.TotalStatistics.class))).thenThrow(new JsonProcessingException("") {});

        // when & then
        assertThatThrownBy(() -> service.find())
                .isInstanceOf(CustomException.class)
                .hasMessage(INTERNAL_SERVER_ERROR.getMessage());
    }

    @DisplayName("json문자열을 직렬화하는 도중에 예외가 발생하면 커스텀 예외를 발생시킨다")
    @Test
    void should_ThrowCustomException_When_SaveThrowsJsonProcessingException() throws JsonProcessingException {
        // given
        GetHomeResponse.TotalStatistics mockStats = mock(GetHomeResponse.TotalStatistics.class);
        when(objectMapper.writeValueAsString(eq(mockStats))).thenThrow(new JsonProcessingException("") {});

        // when & then
        assertThatThrownBy(() -> service.save(mockStats))
                .isInstanceOf(CustomException.class)
                .hasMessage(INTERNAL_SERVER_ERROR.getMessage());
    }


}