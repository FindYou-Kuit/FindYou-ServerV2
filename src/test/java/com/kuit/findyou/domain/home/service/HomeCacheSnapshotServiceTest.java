package com.kuit.findyou.domain.home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
import com.kuit.findyou.domain.home.service.stats.HomeCacheSnapshotService;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.service.CacheSnapshotService;
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
class HomeCacheSnapshotServiceTest {
    @InjectMocks
    private HomeCacheSnapshotService service;

    @Mock
    private CacheSnapshotService cacheSnapshotService;

    @Mock
    private ObjectMapper objectMapper;

    private final String REDIS_CACHE_KEY = "home:statistics";

    @DisplayName("json문자열을 역직렬화하는 도중에 예외가 발생하면 커스텀 예외를 발생시킨다")
    @Test
    void should_ThrowCustomException_When_FindHomeStatsThrowsJsonProcessingException() throws Exception{
        // given
        String json = "{}";
        when(cacheSnapshotService.findJsonCache(eq(REDIS_CACHE_KEY))).thenReturn(Optional.of(json));
        when(objectMapper.readValue(eq(json), eq(GetHomeResponse.TotalStatistics.class))).thenThrow(new JsonProcessingException("") {});

        // when & then
        assertThatThrownBy(() -> service.findHomeStats())
                .isInstanceOf(CustomException.class)
                .hasMessage(INTERNAL_SERVER_ERROR.getMessage());
    }

    @DisplayName("json문자열을 직렬화하는 도중에 예외가 발생하면 커스텀 예외를 발생시킨다")
    @Test
    void should_ThrowCustomException_When_SaveHomeStatsThrowsJsonProcessingException() throws JsonProcessingException {
        // given
        GetHomeResponse.TotalStatistics mockStats = mock(GetHomeResponse.TotalStatistics.class);
        when(objectMapper.writeValueAsString(eq(mockStats))).thenThrow(new JsonProcessingException("") {});

        // when & then
        assertThatThrownBy(() -> service.saveHomeStats(mockStats))
                .isInstanceOf(CustomException.class)
                .hasMessage(INTERNAL_SERVER_ERROR.getMessage());
    }


}