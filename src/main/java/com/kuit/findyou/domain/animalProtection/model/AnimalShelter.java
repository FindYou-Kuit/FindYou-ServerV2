package com.kuit.findyou.domain.animalProtection.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "animal_shelter")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("status = 'Y'")
public class AnimalShelter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location", length = 100, nullable = false)
    private String location;

    @Column(name = "shelter_name", length = 70, nullable = false)
    private String shelterName;
}
