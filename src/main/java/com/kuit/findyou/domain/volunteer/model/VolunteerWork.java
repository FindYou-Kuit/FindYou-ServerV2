package com.kuit.findyou.domain.volunteer.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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
    private LocalDateTime recruitmentStartAt;

    @Column(name = "recruitment_end_at")
    private LocalDateTime recruitmentEndAt;

    @Column(name = "place", length = 255)
    private String place;

    @Column(name = "volunteer_start_at")
    private LocalDateTime volunteerStartAt;

    @Column(name = "volunteer_end_at")
    private LocalDateTime volunteerEndAt;

    @Column(name = "hours", length = 50)
    private String hours;
}
