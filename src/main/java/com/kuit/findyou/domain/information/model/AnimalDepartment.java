package com.kuit.findyou.domain.information.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "animal_departments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnimalDepartment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "organization", length = 100, nullable = false)
    private String organization; // 담당기관

    @Column(name = "department", length = 70, nullable = false)
    private String department; // 담당부서

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber; // 전화번호
}
