package com.kuit.findyou.domain.report.model;

import com.kuit.findyou.domain.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Entity
@DiscriminatorValue("MISSING")
@Table(name = "missing_reports")
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

    @Column(name = "fur_color", length = 100, nullable = false)
    private String furColor;

    @Column(name = "significant", length = 255, nullable = false)
    private String significant;

    @Column(name = "reporter_name", length = 20)
    private String reporterName;

    @Column(name = "reporter_tel", length = 20)
    private String reporterTel;

    @Column(name = "landmark", length = 255, nullable = false)
    private String landmark;

    @Builder
    public MissingReport(String breed, String species, ReportTag tag, LocalDate date,
                          String address, BigDecimal latitude, BigDecimal longitude, User user, Sex sex, String rfid, String age,
                          String furColor, String significant,
                          String reporterName, String reporterTel, String landmark) {
        super(null, breed, species, tag, date, address, latitude, longitude, user, new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        this.sex = sex;
        this.rfid = rfid;
        this.age = age;
        this.furColor = furColor;
        this.significant = significant;
        this.reporterName = reporterName;
        this.reporterTel = reporterTel;
        this.landmark = landmark;
    }

    public static MissingReport createMissingReport(String breed, String species, ReportTag tag, LocalDate date,
                                                    String address, User user, Sex sex, String rfid, String age,
                                                    String furColor, String significant,
                                                    String reporterName, String reporterTel, String landmark, BigDecimal latitude, BigDecimal longitude) {
        MissingReport report = MissingReport.builder()
                .breed(breed)
                .species(species)
                .tag(tag)
                .date(date)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .user(user)
                .sex(sex)
                .rfid(rfid)
                .age(age)
                .furColor(furColor)
                .significant(significant)
                .reporterName(reporterName)
                .reporterTel(reporterTel)
                .landmark(landmark)
                .build();

        if (user != null) {
            user.addReport(report); // 양방향 연관관계 설정
        }

        return report;
    }


}

