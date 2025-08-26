package com.kuit.findyou.domain.information.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.information.model.AnimalDepartment;
import com.kuit.findyou.domain.information.repository.AnimalDepartmentRepository;
import com.kuit.findyou.domain.information.util.DepartmentHtmlParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncAnimalDepartmentServiceTest {

    @InjectMocks
    private SyncAnimalDepartmentServiceImpl service;

    @Mock
    private AnimalDepartmentRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DepartmentHtmlParser parser;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("외부 API 응답이 정상일 때 동기화가 성공한다")
    void should_SynchronizeSuccessfully_When_ExternalApiIsFine() throws Exception {
        // given

        String fakeJson = """
            { "data": [ { "orgdownNm": "송파구", "orgCd": "12345" } ] }
            """;

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(fakeJson);

        AnimalDepartment mockDep = AnimalDepartment.builder()
                .organization("서울특별시 송파구청")
                .department("관광체육과")
                .phoneNumber("02-1234-5678")
                .build();

        when(parser.parse(anyString(), anyString(), anyString()))
                .thenReturn(List.of(mockDep));

        // when
        service.synchronize();

        // then
        verify(repository, times(1)).deleteAllInBatch();
        verify(repository, times(1)).saveAll(anyList());
    }
}
