package com.kuit.findyou.domain.information.service;

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

        List<AnimalDepartment> rows = (district == null || district.isBlank())
                ? animalDepartmentRepository.findAllByIdGreaterThanOrderByIdAsc(cursor, pageable)
                : animalDepartmentRepository.findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(district.trim(), cursor, pageable);

        boolean isLast = rows.size() <= size;
        List<AnimalDepartment> taken = rows.size() > size ? rows.subList(0, size) : rows;
        Long newLastId = taken.isEmpty() ? -1L : taken.get(taken.size() - 1).getId();

        return GetAnimalDepartmentsResponse.from(taken, newLastId, isLast);
    }
}