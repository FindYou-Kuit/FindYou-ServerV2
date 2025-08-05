package com.kuit.findyou.domain.animalProtection.repository;

import com.kuit.findyou.domain.animalProtection.model.AnimalShelter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class AnimalShelterRepositoryTest {
    @Autowired
    private AnimalShelterRepository animalShelterRepository;

    @Test
    @DisplayName("관할구역 + 유형 필터로 병원 조회")
    void findWithFilter_hospitalInJurisdiction() {
        // given
        AnimalShelter s1 = AnimalShelter.builder()
                .shelterName("서울보호소")
                .address("서울특별시 송파구 00로 00길 00")
                .jurisdiction("서울특별시 송파구")
                .phoneNumber("02-123-4567")
                .latitude(37.4979)
                .longitude(127.0276)
                .build();
        AnimalShelter s2 = AnimalShelter.builder()
                .shelterName("송파병원")
                .address("서울특별시 송파구 00로 00길 00")
                .jurisdiction("서울특별시 송파구")
                .phoneNumber("02-000-0000")
                .latitude(37.4979)
                .longitude(127.0276)
                .build();
        AnimalShelter s3 = AnimalShelter.builder()
                .shelterName("강동병원")
                .address("서울특별시 강동구 00로 00길 00")
                .jurisdiction("서울특별시 강동구")
                .phoneNumber("02-000-0000")
                .latitude(37.1212)
                .longitude(128.1212)
                .build();

        animalShelterRepository.saveAll(List.of(s1, s2, s3));

        // when
        List<AnimalShelter> result = animalShelterRepository.findWithFilter(
                0L, "hospital", "병원", "서울특별시 송파구"
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getShelterName()).isEqualTo("송파병원");
    }

    @Test
    @DisplayName("위도/경도 들어있는 보호소, 병원 조회")
    void findAllWithLatLngAfterId() {
        // given
        AnimalShelter s1 = AnimalShelter.builder()
                .shelterName("보호소1")
                .address("서울특별시 강동구 00로 00길 00")
                .jurisdiction("서울특별시 강동구")
                .phoneNumber("02-000-0000")
                .latitude(37.1)
                .longitude(127.1)
                .build();

        AnimalShelter s2 = AnimalShelter.builder()
                .shelterName("보호소2")
                .address("서울특별시 강동구 00로 00길 00")
                .jurisdiction("서울특별시 강동구")
                .phoneNumber("02-000-0000")
                .latitude(36.36)
                .longitude(127.2)
                .build();

        AnimalShelter s3 = AnimalShelter.builder()
                .shelterName("보호소3")
                .address("서울특별시 강동구 00로 00길 00")
                .jurisdiction("서울특별시 강동구")
                .phoneNumber("02-000-0000")
                .latitude(37.3)
                .longitude(127.3)
                .build();

        animalShelterRepository.saveAll(List.of(s1, s2, s3));

        // when
        List<AnimalShelter> result = animalShelterRepository.findAllWithLatLngAfterId(0L);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("shelterName")
                .containsExactly("보호소1", "보호소2", "보호소3");
    }
}
