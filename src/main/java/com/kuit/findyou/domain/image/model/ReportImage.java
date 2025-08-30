package com.kuit.findyou.domain.image.model;

import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "image_url", length = 2083, nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;


    public static ReportImage createReportImage(String imageUrl) {
        ReportImage image = new ReportImage();
        image.imageUrl = imageUrl;
        return image;
    }

    public void setReport(Report report) {
        if (this.report != null && this.report != report) {
            this.report.getReportImages().remove(this);
        }
        this.report = report; //nullable
        if (report != null && !report.getReportImages().contains(this)) {
            report.getReportImages().add(this);
        }
    }
}

