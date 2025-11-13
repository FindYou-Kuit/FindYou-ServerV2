package com.kuit.findyou.domain.information.model;

import com.kuit.findyou.domain.information.dto.request.UpdateVolunteerWorkRequest;
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

    @Column(name = "institution", length = 70)
    private String institution;

    @Column(name = "recruitment_start_date")
    private LocalDate recruitmentStartDate;

    @Column(name = "recruitment_end_date")
    private LocalDate recruitmentEndDate;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "volunteer_start_at")
    private LocalDateTime volunteerStartAt;

    @Column(name = "volunteer_end_at")
    private LocalDateTime volunteerEndAt;

    @Column(name = "web_link", length = 2083)
    private String webLink;

    @Column(name = "register_number", length = 20, unique = true)
    private String registerNumber;

    @Column(name = "run_id")
    private Long runId;

    public void update(UpdateVolunteerWorkRequest request){
        if(request.institution() != null)
            this.institution = request.institution();
        if(request.recruitmentStartDate() != null)
            this.recruitmentStartDate = request.recruitmentStartDate();
        if(request.recruitmentEndDate() != null)
            this.recruitmentEndDate = request.recruitmentEndDate();
        if(request.address() != null)
            this.address = request.address();
        if(request.volunteerStartAt() != null)
            this.volunteerStartAt = request.volunteerStartAt();
        if(request.volunteerEndAt() != null)
            this.volunteerEndAt = request.volunteerEndAt();
        if(request.webLink() != null)
            this.webLink = request.webLink();
        if(request.runId() != null)
            this.runId = request.runId();
    }
}
