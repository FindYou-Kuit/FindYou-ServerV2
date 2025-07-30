package com.kuit.findyou.domain.city.repository;

import com.kuit.findyou.domain.city.model.Sigungu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SigunguRepository extends JpaRepository<Sigungu,Long> {

    // SigunguRepository.java
    List<Sigungu> findBySido_Id(Long sidoId);

}
