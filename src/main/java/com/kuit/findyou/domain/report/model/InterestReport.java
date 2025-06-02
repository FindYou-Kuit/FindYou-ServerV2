package com.kuit.findyou.domain.report.model;

import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "interest_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("status = 'Y'")
public class InterestReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    public static InterestReport createInterestReport(User user, Report report) {
        InterestReport interestReport = new InterestReport();
        interestReport.setUser(user);
        interestReport.setReport(report);
        return interestReport;
    }

    private void setUser(User user) {
        this.user = user;
        user.addInterestReport(this);
    }

    private void setReport(Report report) {
        this.report = report;
        report.addInterestReport(this);
    }

}
