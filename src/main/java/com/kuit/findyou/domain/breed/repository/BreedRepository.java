package com.kuit.findyou.domain.breed.repository;

import com.kuit.findyou.domain.breed.model.Breed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BreedRepository extends JpaRepository<Breed,Long> {
}
