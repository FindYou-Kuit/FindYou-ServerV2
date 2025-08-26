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
    public GetAnimalDepartmentsResponse getDepartments(Long lastId, int size, String sido, String sigungu) {
        String organizationFilter = (sido != null && !sido.isBlank() && sigungu != null && !sigungu.isBlank())
                ? sido + " " + sigungu
                : null;

        List<AnimalDepartment> departments;

        if (organizationFilter == null) {
            departments = animalDepartmentRepository.findAllByIdGreaterThanOrderByDepartmentAsc(
                    lastId == null ? 0L : lastId, PageRequest.of(0, size + 1));
        } else {
            departments = animalDepartmentRepository.findAllByOrganizationContainingAndIdGreaterThanOrderByDepartmentAsc(
                    organizationFilter, lastId == null ? 0L : lastId, PageRequest.of(0, size + 1));
        }

        boolean isLast = departments.size() <= size;
        List<AnimalDepartment> taken = departments.size() > size ? departments.subList(0, size) : departments;
        Long newLastId = taken.isEmpty() ? -1L : taken.get(taken.size() - 1).getId();

        return GetAnimalDepartmentsResponse.from(taken, newLastId, isLast);
    }
}