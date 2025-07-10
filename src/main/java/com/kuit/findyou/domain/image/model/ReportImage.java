package com.kuit.findyou.domain.image.model;

import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Table(name = "report_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("status = 'Y'")
public class ReportImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "uuid", nullable = false)
    private String imageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;


    public static ReportImage createReportImage(String imageUrl, String imageKey) {
        if (imageKey == null || imageKey.isEmpty()) {
            throw new IllegalStateException("UUID cannot be null");
        }
        ReportImage image = new ReportImage();
        image.imageUrl = imageUrl;
        image.imageKey = imageKey;
        return image;
    }

    public void setReport(Report report) {
        if (this.report != null && this.report != report) { //이미 다른 보고서가 등록돼있다면 이미지 제거 -> 다른 보고서로 할당되는 오류 방지
            //this.report.removeImage(this);
            this.report.getReportImages().remove(this);
        }
        this.report = report; //nullable
        if (report != null && !report.getReportImages().contains(this)) {
            //report.addImage(this);
            report.getReportImages().add(this);
        }
    }
}

