package com.kuit.findyou.domain.home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.global.common.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheHomeStatsServiceTest {
    @InjectMocks
    private CacheHomeStatsService service;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    String json = """
                {
                    "recent7days": {
                      "rescuedAnimalCount": "15",
                      "protectingAnimalCount": "42",
                      "adoptedAnimalCount": "8",
                      "lostAnimalCount": "30"
                    },
                    "recent3months": {
                      "rescuedAnimalCount": "120",
                      "protectingAnimalCount": "350",
                      "adoptedAnimalCount": "95",
                      "lostAnimalCount": "210"
                    },
                    "recent1Year": {
                      "rescuedAnimalCount": "520",
                      "protectingAnimalCount": "1200",
                      "adoptedAnimalCount": "430",
                      "lostAnimalCount": "900"
                    }
                  }
                """;

    @Test
    void cacheTotalStatistics_success() throws Exception {
        // given
        GetHomeResponse.TotalStatistics stats = mock(GetHomeResponse.TotalStatistics.class);
        when(objectMapper.writeValueAsString(stats)).thenReturn(json);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        service.cacheTotalStatistics(stats);

        // then
        verify(objectMapper).writeValueAsString(stats);

        // Redis에 저장된 값 확인
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("home:statistics"), captor.capture(), eq(Duration.ofHours(24)));
        assertThat(captor.getValue()).isEqualTo(json);
    }

    @Test
    void should_ThrowException_When_CacheStatsThrowsJsonProcessingException() throws Exception{
        // given
        GetHomeResponse.TotalStatistics stats = GetHomeResponse.TotalStatistics.empty();
        when(objectMapper.writeValueAsString(stats)).thenThrow(new JsonProcessingException("") {});

        // when & then
        assertThatThrownBy(() -> service.cacheTotalStatistics(stats))
                .isInstanceOf(CustomException.class)
                .hasMessage(INTERNAL_SERVER_ERROR.getMessage());

    }

    @Test
    void should_Succeed_When_CachedStatsExists() throws JsonProcessingException {
        // given
        GetHomeResponse.TotalStatistics mockTotalStats = mock(GetHomeResponse.TotalStatistics.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("home:statistics"))).thenReturn(json);
        when(objectMapper.readValue(eq(json), eq(GetHomeResponse.TotalStatistics.class))).thenReturn(mockTotalStats);

        // when
        GetHomeResponse.TotalStatistics cachedStats = service.getCachedTotalStatistics();

        // then
        assertThat(cachedStats).isEqualTo(mockTotalStats);
    }

    @Test
    void should_ThrowCustomException_When_GetCachedStatsThrowsJsonProcessingException() throws Exception{
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("home:statistics"))).thenReturn(json);
        when(objectMapper.readValue(eq(json), eq(GetHomeResponse.TotalStatistics.class))).thenThrow(new JsonProcessingException("") {});

        // when & then
        assertThatThrownBy(() -> service.getCachedTotalStatistics())
                .isInstanceOf(CustomException.class)
                .hasMessage(INTERNAL_SERVER_ERROR.getMessage());

    }
}