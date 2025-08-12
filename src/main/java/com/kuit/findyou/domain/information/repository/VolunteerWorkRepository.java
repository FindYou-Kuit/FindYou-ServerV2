package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.VolunteerWork;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VolunteerWorkRepository extends JpaRepository<VolunteerWork,Long> {
    List<VolunteerWork> findAllByIdLessThanOrderByIdDesc(Long id, Pageable pageable);
}
