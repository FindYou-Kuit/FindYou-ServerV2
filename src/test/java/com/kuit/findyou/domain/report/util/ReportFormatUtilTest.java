package com.kuit.findyou.domain.report.util;

import com.kuit.findyou.domain.report.model.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class ReportFormatUtilTest {

    @Nested
    @DisplayName("formatAge 메서드 검증")
    class FormatAge {
        @Test
        @DisplayName("null 또는 '미상' 이라면 '미상'을 반환한다.")
        void null_or_UNKNOWN_returns_UNKNOWN() {
            assertThat(ReportFormatUtil.formatAge(null)).isEqualTo("미상");
            assertThat(ReportFormatUtil.formatAge("미상")).isEqualTo("미상");
        }

        @Test
        @DisplayName("숫자 값이 주어지면 뒤에 '살'을 붙인다.")
        void append_years_old() {
            assertThat(ReportFormatUtil.formatAge("3")).isEqualTo("3살");
        }
    }

    @Nested
    @DisplayName("formatWeight 메서드 검증")
    class FormatWeight {
        @Test
        @DisplayName("null 또는 '미상' 이라면 '미상'을 반환한다.")
        void null_or_UNKNOWN_returns_UNKNOWN() {
            assertThat(ReportFormatUtil.formatWeight(null)).isEqualTo("미상");
            assertThat(ReportFormatUtil.formatWeight("미상")).isEqualTo("미상");
        }

        @Test
        @DisplayName("숫자 값이 주어지면 뒤에 'kg'을 붙인다.")
        void append_kg() {
            assertThat(ReportFormatUtil.formatWeight("5")).isEqualTo("5kg");
        }
    }

    @Nested
    @DisplayName("formatCoordinate 메서드 검증")
    class FormatCoordinate {
        @Test
        @DisplayName("null 이거나 0.0 이라면 null 을 반환한다.")
        void null_or_zero_returns_null() {
            assertThat(ReportFormatUtil.formatCoordinate(null)).isNull();
            assertThat(ReportFormatUtil.formatCoordinate(BigDecimal.ZERO)).isNull();
            assertThat(ReportFormatUtil.formatCoordinate(BigDecimal.valueOf(0.0))).isNull();
        }

        @Test
        @DisplayName("유효한 좌표(BigDecimal)는 Double 로 변환된다.")
        void valid_coordinate_returns_double() {
            assertThat(ReportFormatUtil.formatCoordinate(BigDecimal.valueOf(127.123)))
                    .isEqualTo(127.123d);
        }
    }

    @Nested
    @DisplayName("safeValue 메서드 검증")
    class SafeValue {
        @Test
        @DisplayName("null 이거나 공백이면 '-' 를 반환한다.")
        void null_or_blank_returns_dash() {
            assertThat(ReportFormatUtil.safeValue(null)).isEqualTo("-");
            assertThat(ReportFormatUtil.safeValue("")).isEqualTo("-");
            assertThat(ReportFormatUtil.safeValue("   ")).isEqualTo("-");
        }

        @Test
        @DisplayName("값이 존재하면 그대로 반환한다.")
        void non_blank_returns_same() {
            assertThat(ReportFormatUtil.safeValue("강남구")).isEqualTo("강남구");
        }
    }

    @Nested
    @DisplayName("safeSex 메서드 검증")
    class SafeSex {
        @Test
        @DisplayName("null 이라면 '-' 를 반환한다.")
        void null_returns_dash() {
            assertThat(ReportFormatUtil.safeSex(null)).isEqualTo("-");
        }

        @Test
        @DisplayName("성별 값이 존재하면 해당 enum 의 value 를 반환한다.")
        void valid_sex_returns_value() {
            assertThat(ReportFormatUtil.safeSex(Sex.M)).isEqualTo("수컷");
            assertThat(ReportFormatUtil.safeSex(Sex.F)).isEqualTo("암컷");
            assertThat(ReportFormatUtil.safeSex(Sex.Q)).isEqualTo("미상");
        }
    }

    @Nested
    @DisplayName("safeDate 메서드 검증")
    class SafeDate {
        @Test
        @DisplayName("2000-01-01 이라면 '-' 를 반환한다.")
        void unknown_date_returns_dash() {
            LocalDate unknown = LocalDate.of(2000, 1, 1);
            assertThat(ReportFormatUtil.safeDate(unknown)).isEqualTo("-");
        }

        @Test
        @DisplayName("그 외의 날짜는 toString() 형식으로 반환한다.")
        void valid_date_returns_string() {
            LocalDate date = LocalDate.of(2025, 8, 26);
            assertThat(ReportFormatUtil.safeDate(date)).isEqualTo("2025-08-26");
        }
    }
}
