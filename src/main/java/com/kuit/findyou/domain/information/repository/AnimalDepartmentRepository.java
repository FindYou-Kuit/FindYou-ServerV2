package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.AnimalDepartment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalDepartmentRepository extends JpaRepository<AnimalDepartment, Long> {
    // 필터링 조건 없이 가나다 순으로 보여줌
    List<AnimalDepartment> findAllByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    // 정확하게 일치하는 경우
    List<AnimalDepartment> findAllByOrganizationEqualsIgnoreCaseAndIdGreaterThanOrderByIdAsc(
            String organization, Long id, Pageable pageable
    );

    // AND 토큰(시도 & 시군구 모두 포함) 사용
    List<AnimalDepartment> findAllByOrganizationContainingIgnoreCaseAndOrganizationContainingIgnoreCaseAndIdGreaterThanOrderByIdAsc(
            String sidoToken, String sigunguToken, Long id, Pageable pageable
    );

    // 전체를 substring으로
    List<AnimalDepartment> findAllByOrganizationContainingIgnoreCaseAndIdGreaterThanOrderByIdAsc(
            String organization, Long id, Pageable pageable
    );
}