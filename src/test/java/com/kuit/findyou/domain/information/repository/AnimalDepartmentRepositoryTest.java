package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.AnimalDepartment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
class AnimalDepartmentRepositoryTest {

    @Autowired
    private AnimalDepartmentRepository repository;

    @Test
    @DisplayName("AnimalDepartment 저장 및 조회 테스트")
    void saveAndFind() {
        AnimalDepartment dep = AnimalDepartment.builder()
                .organization("서울특별시 송파구청")
                .department("관광체육과")
                .phoneNumber("1577-0952")
                .build();

        repository.save(dep);

        List<AnimalDepartment> results = repository
                .findAllByIdGreaterThanOrderByDepartmentAsc(0L, PageRequest.of(0, 10));

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getOrganization()).isEqualTo("서울특별시 송파구청");
    }
}