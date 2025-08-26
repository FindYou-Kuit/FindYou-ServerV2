package com.kuit.findyou.domain.information.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.information.model.AnimalDepartment;
import com.kuit.findyou.domain.information.repository.AnimalDepartmentRepository;
import com.kuit.findyou.domain.information.util.DepartmentHtmlParser;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SyncAnimalDepartmentServiceImpl implements SyncAnimalDepartmentService {
    private final AnimalDepartmentRepository animalDepartmentRepository;
    private final DepartmentHtmlParser parser;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SIGUNGU_API =
            "https://www.animal.go.kr/front/awtis/relevant/selectComOrganTGunList.do";

    public SyncAnimalDepartmentServiceImpl(AnimalDepartmentRepository animalDepartmentRepository,
                                           DepartmentHtmlParser parser,
                                           RestTemplate restTemplate,
                                           ObjectMapper objectMapper) {
        this.animalDepartmentRepository = animalDepartmentRepository;
        this.parser = parser;
        this.restTemplate = (restTemplate != null ? restTemplate : new RestTemplate());
        this.objectMapper = (objectMapper != null ? objectMapper : new ObjectMapper());
    }

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
                // ✅ 한글 파라미터 안전 인코딩
                String url = UriComponentsBuilder.fromHttpUrl(SIGUNGU_API)
                        .queryParam("sido", "{sido}")
                        .build()
                        .expand(sido)
                        .encode(StandardCharsets.UTF_8)
                        .toUriString();

                String jsonResponse = restTemplate.getForObject(url, String.class);
                if (jsonResponse == null || jsonResponse.isBlank()) {
                    log.warn("[synchronize] {} 응답이 비어있음", sido);
                    continue;
                }

                JsonNode data = objectMapper.readTree(jsonResponse).path("data");
                if (!data.isArray()) {
                    log.warn("[synchronize] {} data 배열 아님: {}", sido, data);
                    continue;
                }

                for (JsonNode node : data) {
                    String sigungu = node.path("orgdownNm").asText(null);
                    String orgCd = node.path("orgCd").asText(null);
                    if (sigungu == null || orgCd == null) continue;

                    try {
                        List<AnimalDepartment> parsed = parser.parse(sido, sigungu, orgCd);
                        if (parsed != null && !parsed.isEmpty()) {
                            allDepartments.addAll(parsed);
                            log.info("[synchronize] {} {} 보호부서 {}건 파싱 완료", sido, sigungu, parsed.size());
                        }
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
