package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.AnimalDepartment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnimalDepartmentRepository extends JpaRepository<AnimalDepartment, Long> {
    // 필터링 조건 없이 가나다 순으로 보여줌
    List<AnimalDepartment> findAllByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    // 시군구 필터링
    List<AnimalDepartment> findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(
            String organization, Long id, Pageable pageable
    );
}