package com.kuit.findyou.domain.report.model;

import com.kuit.findyou.domain.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Entity
@DiscriminatorValue("MISSING")
@Table(name = "missing_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissingReport extends Report {

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false)
    private Sex sex;

    @Column(name = "rfid", length = 30)
    private String rfid;

    @Column(name = "age", length = 10, nullable = false)
    private String age;

    @Column(name = "weight")
    private String weight;

    @Column(name = "fur_color", nullable = false)
    private String furColor;

    @Column(name = "significant", nullable = false)
    private String significant;

    @Column(name = "reporter_info", nullable = false)
    private String reporterInfo;

    @Column(name = "landmark", nullable = false)
    private String landmark;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal longitude;

    @Builder
    private MissingReport(String breed, String species, ReportTag tag, LocalDate date,
                          String address, User user, Sex sex, String rfid, String age,
                          String weight, String furColor, String significant,
                          String reporterInfo, String landmark, BigDecimal latitude,
                          BigDecimal longitude) {
        super(null, breed, species, tag, date, address, user, new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());
        this.sex = sex;
        this.rfid = rfid;
        this.age = age;
        this.weight = weight;
        this.furColor = furColor;
        this.significant = significant;
        this.reporterInfo = reporterInfo;
        this.landmark = landmark;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}

