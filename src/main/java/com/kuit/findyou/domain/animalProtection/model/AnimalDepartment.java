package com.kuit.findyou.domain.animalProtection.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "animal_department")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("status = 'Y'")
public class AnimalDepartment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization", length = 100, nullable = false)
    private String organization;

    @Column(name = "department", length = 70, nullable = false)
    private String department;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;
}
