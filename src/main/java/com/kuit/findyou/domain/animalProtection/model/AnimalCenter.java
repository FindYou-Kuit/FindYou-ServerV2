package com.kuit.findyou.domain.animalProtection.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "animal_center")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("status = 'Y'")
public class AnimalCenter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "jurisdiction", length = 100, nullable = false)
    private String jurisdiction;

    @Column(name = "center_name", length = 70, nullable = false)
    private String name;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "address", length = 255, nullable = false)
    private String address;
}
