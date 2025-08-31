package com.kuit.findyou.domain.information.service.animalDepartment;

import com.kuit.findyou.domain.information.dto.GetAnimalDepartmentsResponse;
import com.kuit.findyou.domain.information.model.AnimalDepartment;
import com.kuit.findyou.domain.information.repository.AnimalDepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimalDepartmentServiceImpl implements AnimalDepartmentService {

    private final AnimalDepartmentRepository animalDepartmentRepository;

    @Override
    public GetAnimalDepartmentsResponse getDepartments(Long lastId, int size, String district) {
        Long cursor = (lastId == null ? 0L : lastId);
        var pageable = PageRequest.of(0, size + 1);

        String norm = (district == null || district.isBlank()) ? null : district.trim();

        List<AnimalDepartment> rows;

        if (norm == null) {
            rows = animalDepartmentRepository.findAllByIdGreaterThanOrderByIdAsc(cursor, pageable);
        } else {
            // 정확하게 일치하는 것을 우선으로
            rows = animalDepartmentRepository.findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(norm, cursor, pageable);

            // AND 토큰(시도/시군구 모두 포함)
            if (rows.isEmpty()) {
                String[] tokens = norm.split("\\s+", 2); // [0]=시도, [1]=나머지(시군구 등)
                if (tokens.length == 2) {
                    rows = animalDepartmentRepository.findAllByOrganizationContainingAndOrganizationContainingAndIdGreaterThanOrderByIdAsc(
                            tokens[0], tokens[1], cursor, pageable
                    );
                }
            }
            // 전체 문자열 substring 폴백
            if (rows.isEmpty()) {
                rows = animalDepartmentRepository.findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(norm, cursor, pageable);
            }
        }

        boolean isLast = rows.size() <= size;
        List<AnimalDepartment> taken = isLast ? rows : rows.subList(0, size);
        Long newLastId = (isLast || taken.isEmpty()) ? null : taken.get(taken.size() - 1).getId();

        return GetAnimalDepartmentsResponse.from(taken, newLastId, isLast);
    }
}