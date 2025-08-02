package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.AnimalDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalDepartmentRepository extends JpaRepository<AnimalDepartment,Long> {
}
