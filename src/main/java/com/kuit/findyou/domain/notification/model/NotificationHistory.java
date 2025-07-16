package com.kuit.findyou.domain.notification.model;

import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "notification_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_history_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_viewed", nullable = false, columnDefinition = "CHAR(1)")
    private ViewStatus isViewed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    public static NotificationHistory createNotificationHistory(User user, Report report, ViewStatus isViewed) {
        NotificationHistory history = NotificationHistory.builder()
                .isViewed(isViewed)
                .user(user)
                .report(report)
                .build();
        user.addNotificationHistory(history);
        report.addNotificationHistory(history);
        return history;
    }
}
