package com.kuit.findyou.domain.information.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.information.model.AnimalDepartment;
import com.kuit.findyou.domain.information.repository.AnimalDepartmentRepository;
import com.kuit.findyou.domain.information.util.DepartmentHtmlParser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SyncAnimalDepartmentServiceImpl implements SyncAnimalDepartmentService {
    private final AnimalDepartmentRepository animalDepartmentRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SIGUNGU_API =
            "https://www.animal.go.kr/front/awtis/relevant/selectComOrganTGunList.do?sido=";

    @Override
    @Transactional
    public void synchronize() {
        log.info("[synchronize] 보호부서 동기화 시작");

        try {
            List<String> sidos = List.of(
                    "서울특별시", "부산광역시", "대구광역시", "인천광역시",
                    "광주광역시", "대전광역시", "울산광역시", "세종특별자치시",
                    "경기도", "강원도", "충청북도", "충청남도",
                    "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"
            );

            List<AnimalDepartment> allDepartments = new ArrayList<>();

            for (String sido : sidos) {
                String jsonResponse = restTemplate.getForObject(SIGUNGU_API + sido, String.class);
                JsonNode root = objectMapper.readTree(jsonResponse).get("data");

                for (JsonNode node : root) {
                    String sigungu = node.get("orgdownNm").asText();
                    String orgCd = node.get("orgCd").asText();

                    try {
                        List<AnimalDepartment> parsed = DepartmentHtmlParser.parse(sido, sigungu, orgCd);
                        allDepartments.addAll(parsed);

                        log.info("[synchronize] {} {} 보호부서 {}건 파싱 완료", sido, sigungu, parsed.size());
                    } catch (Exception e) {
                        log.error("[synchronize] {} {} 파싱 실패: {}", sido, sigungu, e.getMessage());
                    }
                }
            }

            animalDepartmentRepository.deleteAllInBatch();
            animalDepartmentRepository.saveAll(allDepartments);

            log.info("[synchronize] 보호부서 동기화 완료 (총 {}건)", allDepartments.size());

        } catch (Exception e) {
            log.error("[synchronize] 보호부서 동기화 전체 실패: {}", e.getMessage(), e);
        }
    }

}
