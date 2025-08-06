package com.kuit.findyou.domain.city.repository;

import com.kuit.findyou.domain.city.model.Sido;
import com.kuit.findyou.domain.city.model.Sigungu;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@Transactional
@ActiveProfiles("test")
class SigunguRepositoryTest {

    @Autowired
    SigunguRepository sigunguRepository;

    @Autowired
    SidoRepository sidoRepository;

    @Autowired
    EntityManager em;

    private Sido savedSido;

    @BeforeEach
    void setUp() {
        savedSido = sidoRepository.save(Sido.builder()
                .name("서울특별시")
                .build());

        List<Sigungu> sigungus = List.of(
                Sigungu.builder().name("광진구").sido(savedSido).build(),
                Sigungu.builder().name("마포구").sido(savedSido).build(),
                Sigungu.builder().name("영등포구").sido(savedSido).build()
        );

        sigunguRepository.saveAll(sigungus);
    }

    @Test
    @DisplayName("시/도 ID로 시/군/구 목록을 조회하면 해당 시/도에 속한 시/군/구 3개가 반환된다")
    void findBySidoId() {
        // when
        List<Sigungu> result = sigunguRepository.findBySidoId(savedSido.getId());

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Sigungu::getName)
                .containsExactlyInAnyOrder("광진구", "마포구", "영등포구");

        assertThat(result).allSatisfy(sigungu ->
                assertThat(sigungu.getSido().getName()).isEqualTo("서울특별시"));
    }
}
