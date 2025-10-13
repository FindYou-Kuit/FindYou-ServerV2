package com.kuit.findyou.domain.information.service.animalDepartment;

import com.kuit.findyou.domain.information.dto.response.GetAnimalDepartmentsResponse;

public interface AnimalDepartmentService {
    GetAnimalDepartmentsResponse getDepartments(Long lastId, int size, String district);
}
