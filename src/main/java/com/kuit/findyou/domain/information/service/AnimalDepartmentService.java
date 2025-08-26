package com.kuit.findyou.domain.information.service;

import com.kuit.findyou.domain.information.dto.GetAnimalDepartmentsResponse;

public interface AnimalDepartmentService {
    GetAnimalDepartmentsResponse getDepartments(Long lastId, int size, String sido, String sigungu);
}
