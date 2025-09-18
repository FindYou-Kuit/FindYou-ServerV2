package com.kuit.findyou.domain.home.validation;

import com.kuit.findyou.domain.home.dto.GetHomeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class GetHomeRequestValidatorTest {

    private GetHomeRequestValidator getHomeRequestValidator = new GetHomeRequestValidator();

    @DisplayName("위도가 null이면 false를 반환한다")
    @Test
    void shouldReturnFalse_whenLatIsNull() {
        assertThat(getHomeRequestValidator.isValid(new GetHomeRequest(null, 135.0), null)).isFalse();
    }

    @DisplayName("경도가 null이면 false를 반환한다")
    @Test
    void shouldReturnFalse_whenLngIsNull() {
        assertThat(getHomeRequestValidator.isValid(new GetHomeRequest(35.0, null), null)).isFalse();
    }

    @DisplayName("위도 경도 둘 다 null이면 true를 반환한다")
    @Test
    void shouldReturnTrue_whenLatAndLngAreNull() {
        assertThat(getHomeRequestValidator.isValid(new GetHomeRequest(null, null), null)).isTrue();
    }

    @DisplayName("범위 밖에 있는 위도와 경도면 false를 반환한다")
    @ParameterizedTest(name = "[{index}] invalid out of range: lat={0}, lng={1}")
    @CsvSource({
            "-90.0001, 0.0",
            "90.0001, 0.0",
            "0.0, -180.0001",
            "0.0, 180.0001"
    })
    void shouldReturnFalse_whenLatAndLngOutOfRange(Double lat, Double lng) {
        assertThat(getHomeRequestValidator.isValid(new GetHomeRequest(lat, lng), null)).isFalse();
    }

    @DisplayName("범위 내에 있는 위도와 경도면 true를 반환한다")
    @ParameterizedTest(name = "[{index}] valid inside range: lat={0}, lng={1}")
    @CsvSource({
            "37.5, 127.0",
            "-90.0, 0.0",
            "90.0, 0.0",
            "0.0, -180.0",
            "0.0, 180.0"
    })
    void shouldReturnTrue_whenLatAndLngWithinRange(Double lat, Double lng) {
        assertThat(getHomeRequestValidator.isValid(new GetHomeRequest(lat, lng), null)).isTrue();
    }

}