package com.kuit.findyou.domain.breed.repository;

import com.kuit.findyou.domain.breed.model.Breed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BreedRepository extends JpaRepository<Breed,Long> {

    // 강아지 품종들 조회
    @Query("SELECT b FROM Breed b WHERE b.species = '강아지'")
    List<Breed> findAllDogBreeds();

    // 고양이 품종들 조회
    @Query("SELECT b FROM Breed b WHERE b.species = '고양이'")
    List<Breed> findAllCatBreeds();

    // 기타 품종들 조회
    @Query("SELECT b FROM Breed b WHERE b.species = '기타'")
    List<Breed> findAllEtcBreeds();
}
