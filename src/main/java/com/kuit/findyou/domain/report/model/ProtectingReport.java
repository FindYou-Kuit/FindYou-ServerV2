package com.kuit.findyou.domain.report.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kuit.findyou.domain.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Entity
@DiscriminatorValue("PROTECTING")
@Table(name = "protecting_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProtectingReport extends Report {

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "CHAR(1)", nullable = false)
    private Sex sex;

    @Column(name = "age", length = 10, nullable = false)
    private String age;

    @Column(name = "weight", length = 10, nullable = false)
    private String weight;

    @Column(name = "fur_color", length = 30, nullable = false)
    private String furColor;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "CHAR(1)", nullable = false)
    private Neutering neutering;

    @Column(name = "significant", length = 200, nullable = false)
    private String significant;

    @Column(name = "found_location", length = 100, nullable = false)
    private String foundLocation;

    @Column(name = "notice_number", length = 30, nullable = false)
    private String noticeNumber;

    @Column(name = "notice_start_date", columnDefinition = "DATE", nullable = false)
    private LocalDate noticeStartDate;

    @Column(name = "notice_end_date", columnDefinition = "DATE", nullable = false)
    private LocalDate noticeEndDate;

    @Column(name = "care_name", length = 50, nullable = false)
    private String careName;

    @Column(name = "care_tel", length = 14, nullable = false)
    private String careTel;

    @Column(name = "authority", length = 50, nullable = false)
    private String authority;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal longitude;

    @Builder
    public ProtectingReport(String breed, String species, ReportTag tag, LocalDate date,
                             String address, User user, Sex sex, String age, String weight,
                             String furColor, Neutering neutering, String significant,
                             String foundLocation, String noticeNumber, LocalDate noticeStartDate,
                             LocalDate noticeEndDate, String careName,
                             String careTel, String authority, BigDecimal latitude,
                            BigDecimal longitude) {
        super(null, breed, species, tag, date, address, user, new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        this.sex = sex;
        this.age = age;
        this.weight = weight;
        this.furColor = furColor;
        this.neutering = neutering;
        this.significant = significant;
        this.foundLocation = foundLocation;
        this.noticeNumber = noticeNumber;
        this.noticeStartDate = noticeStartDate;
        this.noticeEndDate = noticeEndDate;
        this.careName = careName;
        this.careTel = careTel;
        this.authority = authority;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static ProtectingReport createProtectingReport(String breed, String species, ReportTag tag,
                                                          LocalDate date, String address, User user,
                                                          Sex sex, String age, String weight,
                                                          String furColor, Neutering neutering,
                                                          String significant, String foundLocation,
                                                          String noticeNumber, LocalDate noticeStartDate,
                                                          LocalDate noticeEndDate, String careName,
                                                          String careTel, String authority,
                                                          BigDecimal latitude, BigDecimal longitude) {

        ProtectingReport report = ProtectingReport.builder()
                .breed(breed)
                .species(species)
                .tag(tag)
                .date(date)
                .address(address)
                .user(user)
                .sex(sex)
                .age(age)
                .weight(weight)
                .furColor(furColor)
                .neutering(neutering)
                .significant(significant)
                .foundLocation(foundLocation)
                .noticeNumber(noticeNumber)
                .noticeStartDate(noticeStartDate)
                .noticeEndDate(noticeEndDate)
                .careName(careName)
                .careTel(careTel)
                .authority(authority)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        if (user != null) {
            user.addReport(report); // 양방향 연관관계 설정
        }

        return report;
    }

    @JsonIgnore
    public String getNoticeDuration() {
        return noticeStartDate + " ~ " + noticeEndDate;
    }

}



