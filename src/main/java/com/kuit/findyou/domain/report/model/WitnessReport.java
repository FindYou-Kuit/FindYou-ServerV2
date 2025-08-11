package com.kuit.findyou.domain.report.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import com.kuit.findyou.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@DiscriminatorValue("WITNESS")
@Table(name = "witness_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WitnessReport extends Report {

    @Column(name = "fur_color", length = 100, nullable = false)
    private String furColor;

    @Column(name = "significant", length = 255, nullable = false)
    private String significant;

    @Column(name = "reporter_name", length = 50)
    private String reporterName;

    @Column(name = "landmark", length = 255, nullable = false)
    private String landmark;

    @Builder
    public WitnessReport(String breed, String species, ReportTag tag, LocalDate date,
                          String address, BigDecimal latitude, BigDecimal longitude, User user, String furColor, String significant,
                          String reporterName, String landmark) {
        super(null, breed, species, tag, date, address, latitude, longitude, user, new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        this.furColor = furColor;
        this.significant = significant;
        this.reporterName = reporterName;
        this.landmark = landmark;
    }

    public static WitnessReport createWitnessReport(String breed, String species, ReportTag tag, LocalDate date,
                                                    String address, User user, String furColor, String significant,
                                                    String reporterName, String landmark, BigDecimal latitude, BigDecimal longitude) {
        WitnessReport report = WitnessReport.builder()
                .breed(breed)
                .species(species)
                .tag(tag)
                .date(date)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .user(user)
                .furColor(furColor)
                .significant(significant)
                .reporterName(reporterName)
                .landmark(landmark)
                .build();

        if (user != null) {
            user.addReport(report); // 양방향 연관관계 설정
        }

        return report;
    }


}

