package com.kuit.findyou.domain.recommend.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;


@Entity
@Table(name = "recommend_video")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("status = 'Y'")
public class RecommendVideo extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "link", length = 255, nullable = false)
    private String link;

    @Column(name = "uploader", length = 100, nullable = false)
    private String uploader;
}
