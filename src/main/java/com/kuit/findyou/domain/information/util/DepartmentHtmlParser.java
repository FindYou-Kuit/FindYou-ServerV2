package com.kuit.findyou.domain.information.util;

import com.kuit.findyou.domain.information.model.AnimalDepartment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DepartmentHtmlParser {
    private static final String RELEVANT_LIST_API =
            "https://www.animal.go.kr/front/awtis/relevant/relevantList.do?menuNo=5000000014";

    // 운영용: Jsoup으로 사이트 직접 호출
    public List<AnimalDepartment> parse(String sido, String sigungu, String orgCd) throws Exception {
        String url = RELEVANT_LIST_API + "&sido=" + sido + "&sigungu=" + sigungu + "&orgCd=" + orgCd;
        Document doc = Jsoup.connect(url).get();
        return extractDepartments(doc);
    }

    // 테스트/공용용: HTML 문자열만 받아 파싱
    public List<AnimalDepartment> parseHtml(String sido, String sigungu, String orgCd, String html) {
        Document doc = Jsoup.parse(html);
        return extractDepartments(doc);
    }

    // 공통 로직
    private List<AnimalDepartment> extractDepartments(Document doc) {
        Elements rows = doc.select("table tbody tr");
        List<AnimalDepartment> departments = new ArrayList<>();

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 3) continue;

            String organization = cols.get(0).text(); // 기관명
            String department = cols.get(1).text();   // 담당부서
            String phoneNumber = cols.get(2).text();  // 전화번호

            departments.add(AnimalDepartment.builder()
                    .organization(organization)
                    .department(department)
                    .phoneNumber(phoneNumber)
                    .build()
            );
        }
        return departments;
    }
}
