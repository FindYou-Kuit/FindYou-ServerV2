package com.kuit.findyou.domain.volunteer.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "volunteer_works")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VolunteerWork extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "recruitment_start_at")
    private LocalDate recruitmentStartAt;

    @Column(name = "recruitment_end_at")
    private LocalDate recruitmentEndAt;

    @Column(name = "place", length = 255)
    private String place;

    @Column(name = "volunteer_start_at")
    private LocalDate volunteerStartAt;

    @Column(name = "volunteer_end_at")
    private LocalDate volunteerEndAt;

    @Column(name = "volunteerTime", length = 50)
    private String volunteerTime;
}
