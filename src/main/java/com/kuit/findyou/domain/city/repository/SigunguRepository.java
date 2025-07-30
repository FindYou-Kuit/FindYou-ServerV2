package com.kuit.findyou.domain.city.repository;

import com.kuit.findyou.domain.city.model.Sigungu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SigunguRepository extends JpaRepository<Sigungu,Long> {

    @Query(value = "SELECT * FROM sigungus WHERE sido_id = :sidoId", nativeQuery = true)
    List<Sigungu> findBySidoId(@Param("sidoId") Long sidoId);

}
