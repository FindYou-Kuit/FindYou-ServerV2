package com.kuit.findyou.domain.animalProtection.service;

import com.kuit.findyou.domain.information.dto.AnimalShelterPagingResponse;
import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.information.model.AnimalShelter;
import com.kuit.findyou.domain.information.repository.AnimalShelterRepository;
import com.kuit.findyou.domain.information.service.AnimalShelterServiceImpl;
import com.kuit.findyou.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AnimalShelterServiceImplTest {
    @Mock
    private AnimalShelterRepository animalShelterRepository;

    @InjectMocks
    private AnimalShelterServiceImpl animalShelterService;

    @Test
    @DisplayName("관할구역 필터 + 유형(hospital) 필터 성공")
    void getShelters_validFilter() {
        AnimalShelter hospital = AnimalShelter.builder()
                .id(10L)
                .shelterName("OO병원")
                .jurisdiction("서울특별시 송파구")
                .phoneNumber("02-123-4567")
                .address("서울특별시 송파구 어딘가")
                .latitude(37.5)
                .longitude(127.1)
                .build();
        int size = 10;

        when(animalShelterRepository.findWithFilter(0L, "hospital", "병원", "서울특별시 송파구", PageRequest.of(0, size+1)))
                .thenReturn(List.of(hospital));

        AnimalShelterPagingResponse<AnimalShelterResponse> result = animalShelterService.getShelters(0L, "hospital", "서울특별시", "송파구", null, null, size);

        assertThat(result.centers()).hasSize(1);
        assertThat(result.centers().get(0).centerName()).isEqualTo("OO병원");
        assertThat(result.isLast()).isTrue();
        assertThat(result.lastId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("위치 기반 조회 - 반경 7km 이내만 반환")
    void getNearbyCenters_withinRadiusOnly() {
        AnimalShelter s1 = AnimalShelter.builder()
                .id(1L)
                .shelterName("가까운 병원")
                .jurisdiction("서울특별시 송파구")
                .phoneNumber("02-123-4567")
                .address("서울특별시 송파구 어딘가")
                .latitude(37.5)
                .longitude(127.1)
                .build();

        AnimalShelter s2 = AnimalShelter.builder()
                .id(2L)
                .shelterName("멀리 있는 보호소")
                .jurisdiction("서울특별시 송파구")
                .phoneNumber("02-123-4567")
                .address("서울특별시 송파구 어딘가")
                .latitude(37.0)
                .longitude(128.0)
                .build();

        int size = 10;

        when(animalShelterRepository.findAllWithLatLngAfterId(0L, PageRequest.of(0, size+1)))
                .thenReturn(List.of(s1, s2));

        AnimalShelterPagingResponse<AnimalShelterResponse> result = animalShelterService.getNearbyCenters(0L, 37.5, 127.1, size);

        assertThat(result.centers()).hasSize(1);
        assertThat(result.centers().get(0).centerName()).isEqualTo("가까운 병원");
        assertThat(result.isLast()).isTrue();
        assertThat(result.lastId()).isEqualTo(1L);
    }

}
