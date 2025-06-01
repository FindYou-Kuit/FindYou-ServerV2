package com.kuit.findyou.domain.recommend.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;


@Entity
@Table(name = "recommend_article")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("status = 'Y'")
public class RecommendArticle extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "source", length = 255, nullable = false)
    private String source;

    @Column(name = "link", length = 255, nullable = false)
    private String link;
}
