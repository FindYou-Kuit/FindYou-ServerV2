package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.AnimalShelter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimalShelterRepository extends JpaRepository<AnimalShelter,Long> {
    @Query("""
    SELECT a FROM AnimalShelter a
    WHERE a.id > :lastId
      AND (:type = 'all' 
           OR (:type = 'hospital' AND a.shelterName LIKE %:hospital%)
           OR (:type = 'shelter' AND a.shelterName NOT LIKE %:hospital%))
      AND (:jurisdiction IS NULL OR a.jurisdiction LIKE %:jurisdiction%)
    ORDER BY a.id ASC
    """)
    List<AnimalShelter> findWithFilter(@Param("lastId") Long lastId, @Param("type") String type, @Param("jurisdiction") String jurisdiction);

}
