package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.AnimalShelter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimalShelterRepository extends JpaRepository<AnimalShelter,Long> {
    @Query("""
    SELECT a FROM AnimalShelter a
    WHERE (:lastId IS NULL OR a.id > :lastId)
      AND (
          :type = 'all' 
          OR (:type = 'hospital' AND a.shelterName LIKE CONCAT('%', :hospital, '%'))
          OR (:type = 'shelter' AND a.shelterName NOT LIKE CONCAT('%', :hospital, '%'))
      )
      AND (:jurisdiction IS NULL OR a.jurisdiction LIKE CONCAT('%', :jurisdiction, '%'))
    ORDER BY a.id ASC
""")
    List<AnimalShelter> findWithFilter(@Param("lastId") Long lastId,
                                       @Param("type") String type,
                                       @Param("hospital") String hospital,
                                       @Param("jurisdiction") String jurisdiction,
                                       Pageable pageSize
    );

    @Query("""
    SELECT a FROM AnimalShelter a
    WHERE (:lastId IS NULL OR a.id > :lastId)
      AND a.latitude IS NOT NULL
      AND a.longitude IS NOT NULL
    ORDER BY a.id ASC
""")
    List<AnimalShelter> findAllWithLatLngAfterId(@Param("lastId") Long lastId, Pageable pageable);

}
