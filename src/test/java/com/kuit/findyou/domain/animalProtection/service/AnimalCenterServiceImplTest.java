package com.kuit.findyou.domain.animalProtection.service;

import com.kuit.findyou.domain.information.dto.AnimalCenterPagingResponse;
import com.kuit.findyou.domain.information.dto.AnimalCenterResponse;
import com.kuit.findyou.domain.information.model.AnimalCenter;
import com.kuit.findyou.domain.information.repository.AnimalCenterRepository;
import com.kuit.findyou.domain.information.service.animalCenter.AnimalCenterServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AnimalCenterServiceImplTest {
    @Mock
    private AnimalCenterRepository animalCenterRepository;

    @InjectMocks
    private AnimalCenterServiceImpl animalCenterService;

    @Test
    @DisplayName("관할구역 필터  성공")
    void getCenters_validFilter() {
        AnimalCenter hospital = AnimalCenter.builder()
                .id(10L)
                .name("OO병원")
                .jurisdiction("서울특별시 송파구")
                .phoneNumber("02-123-4567")
                .address("서울특별시 송파구 어딘가")
                .latitude(37.5)
                .longitude(127.1)
                .build();
        int size = 10;

        when(animalCenterRepository.findWithFilter(0L, "서울특별시 송파구", PageRequest.of(0, size+1)))
                .thenReturn(List.of(hospital));

        AnimalCenterPagingResponse<AnimalCenterResponse> result = animalCenterService.getCenters(0L, "서울특별시 송파구", size);

        assertThat(result.centers()).hasSize(1);
        assertThat(result.centers().get(0).centerName()).isEqualTo("OO병원");
        assertThat(result.isLast()).isTrue();
        assertThat(result.lastId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("위치 기반 조회 - 반경 3km 이내만 반환")
    void getNearbyCenters_withinRadiusOnly() {
        AnimalCenter s1 = AnimalCenter.builder()
                .id(1L)
                .name("가까운 보호소")
                .jurisdiction("서울특별시 송파구")
                .phoneNumber("02-123-4567")
                .address("서울특별시 송파구 어딘가")
                .latitude(37.5)
                .longitude(127.1)
                .build();

        AnimalCenter s2 = AnimalCenter.builder()
                .id(2L)
                .name("멀리 있는 보호소")
                .jurisdiction("서울특별시 송파구")
                .phoneNumber("02-123-4567")
                .address("서울특별시 송파구 어딘가")
                .latitude(37.0)
                .longitude(128.0)
                .build();

        int size = 10;

        when(animalCenterRepository.findAllWithLatLngAfterId(0L, PageRequest.of(0, size+1)))
                .thenReturn(List.of(s1, s2));

        AnimalCenterPagingResponse<AnimalCenterResponse> result = animalCenterService.getNearbyCenters(0L, 37.5, 127.1, size);

        assertThat(result.centers()).hasSize(1);
        assertThat(result.centers().get(0).centerName()).isEqualTo("가까운 보호소");
        assertThat(result.isLast()).isTrue();
        assertThat(result.lastId()).isEqualTo(1L);
    }

}
