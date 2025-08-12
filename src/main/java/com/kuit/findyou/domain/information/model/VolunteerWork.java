package com.kuit.findyou.domain.information.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @Column(name = "institution", length = 70)
    private String institution;

    @Column(name = "recruitment_start_at")
    private LocalDate recruitmentStartAt;

    @Column(name = "recruitment_end_at")
    private LocalDate recruitmentEndAt;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "volunteer_start_at")
    private LocalDate volunteerStartAt;

    @Column(name = "volunteer_end_at")
    private LocalDate volunteerEndAt;

    @Column(name = "volunteer_time", length = 50)
    private String volunteerTime;

    @Column(name = "web_link", length = 2083)
    private String webLink;
}
