package com.kuit.findyou.domain.report.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.notification.model.NotificationHistory;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public abstract class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "breed", length = 20, nullable = false)
    private String breed;

    @Column(name = "species", length = 100, nullable = false)
    private String species;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag", length = 20, nullable = false)
    private ReportTag tag;

    // 실종 : 실종 날짜
    // 목격 : 목격 날짜
    // 보호 : 발견 날짜
    @Column(name = "date", nullable = false, columnDefinition = "DATE")
    private LocalDate date;

    // 실종 : 실종 장소
    // 목격 : 목격 장소
    // 보호 : 보호 장소 => 보호소 주소 (care_addr)
    @Column(name = "address", length = 200, nullable = false)
    private String address;

    @Column(precision = 9, scale = 6)
    @Setter
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    @Setter
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Version // 글 상세 조회 시 위도,경도 값을 획득해 올 때, 동시성 처리를 위한 낙관적 락 도입
    @JsonIgnore
    @Column(name = "version", nullable = false)
    private Long version;

    // 최근 본 글 삭제를 위한 양방향 연관관계 설정
    // orphanRemoval = true 만 설정
    @OneToMany(mappedBy = "report", orphanRemoval = true)
    private List<ViewedReport> viewedReports = new ArrayList<>();

    // 관심글 삭제를 위한 양방향 연관관계 설정
    // orphanRemoval = true 만 설정
    @OneToMany(mappedBy = "report", orphanRemoval = true)
    private List<InterestReport> interestReports = new ArrayList<>();

    // 글 이미지 삭제를 위한 양방향 연관관계 설정
    // orphanRemoval = true 만 설정
    @OneToMany(mappedBy = "report", orphanRemoval = true)
    private List<ReportImage> reportImages = new ArrayList<>();

    // 알림 내역 삭제를 위한 양방향 연관관계 설정
    // orphanRemoval = true 만 설정
    @OneToMany(mappedBy = "report", orphanRemoval = true)
    private List<NotificationHistory> notificationHistories = new ArrayList<>();

    // 연관 관계 편의 메서드
    public void addViewedReport(ViewedReport viewedReport) {
        viewedReports.add(viewedReport);
    }

    public void addInterestReport(InterestReport interestReport) {
        interestReports.add(interestReport);
    }

    public void addNotificationHistory(NotificationHistory history) { notificationHistories.add(history); }

    @JsonIgnore
    public List<String> getReportImagesUrlList() {
        return reportImages.stream()
                .map(ReportImage::getImageUrl)
                .toList();
    }

    @JsonIgnore
    public boolean isCoordinatesAbsent() {
        return latitude == null || longitude == null;
    }
}
