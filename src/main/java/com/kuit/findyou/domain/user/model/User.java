package com.kuit.findyou.domain.user.model;

import com.kuit.findyou.domain.notification.model.NotificationHistory;
import com.kuit.findyou.domain.notification.model.ReceiveNotification;
import com.kuit.findyou.domain.report.model.InterestReport;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ViewedReport;
import com.kuit.findyou.domain.subscribe.model.Subscribe;
import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE users SET status = 'N' WHERE user_id = ?")
@SQLRestriction("status = 'Y'")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Lob
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "kakao_id", nullable = false)
    private Long kakaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(columnDefinition = "CHAR(1)", nullable = false, length = 1)
    private ReceiveNotification  receiveNotification = ReceiveNotification.N;


    // 신고글에 대해 orphanRemoval = true 만 설정
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Builder.Default
    private List<Report> reports = new ArrayList<>();

    // 최근 본 글 과의 양방향 연관 관계 설정
    // 최근 본 글에 대해 orphanRemoval = true 만 설정
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Builder.Default
    private List<ViewedReport> viewedReports = new ArrayList<>();

    // 관심글 과의 양방향 연관 관계 설정
    // 관심글에 대해 orphanRemoval = true 만 설정
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Builder.Default
    private List<InterestReport> interestReports = new ArrayList<>();

    // 알림 내역 과의 양방향 연관 관계 설정
    // 알림 내역에 대해 orphanRemoval = true 만 설정
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Builder.Default
    private List<NotificationHistory> notificationHistories = new ArrayList<>();

    // 구독 과의 양방향 연관 관계 설정
    // 구독에 대해 orphanRemoval = true 만 설정
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Builder.Default
    private List<Subscribe> subscribes = new ArrayList<>();

    public void addReport(Report report) {
        reports.add(report);
    }

    public void addViewedReport(ViewedReport viewedReport) {
        viewedReports.add(viewedReport);
    }

    public void addInterestReport(InterestReport interestReport) {
        interestReports.add(interestReport);
    }
    public void addNotificationHistory(NotificationHistory notificationHistory) { notificationHistories.add(notificationHistory); }
    public void addSubscribe(Subscribe subscribe) { subscribes.add(subscribe); }

}
