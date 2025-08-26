package com.kuit.findyou.domain.information.util;

import com.kuit.findyou.domain.information.model.AnimalDepartment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentHtmlParserTest {

    private final DepartmentHtmlParser parser = new DepartmentHtmlParser();

    @Test
    @DisplayName("부서 HTML 파서가 실제 마크업(픽스처)을 정확히 파싱한다")
    void parse_realFixtureHtml_success() throws Exception {
        // given: 픽스처 HTML 파일 로드
        String html = Files.readString(
                Path.of("src/test/resources/fixtures/department/sungpa.html")
        );

        // DepartmentHtmlParser에 parseHtml 오버로드 추가했다고 가정
        // (sido, sigungu, orgCd, html) → List<AnimalDepartment>
        List<AnimalDepartment> result = parser.parseHtml("서울특별시", "송파구", "12345", html);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getOrganization()).contains("송파구");
        assertThat(result.get(0).getDepartment()).isEqualTo("관광체육과");
        assertThat(result.get(0).getPhoneNumber()).matches("\\d{2,3}-\\d{3,4}-\\d{4}");
    }
}
