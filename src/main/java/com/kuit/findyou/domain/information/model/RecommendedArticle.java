package com.kuit.findyou.domain.information.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "recommended_articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecommendedArticle extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", length = 2083, nullable = false)
    private String title;

    @Column(name = "source", length = 2083, nullable = false)
    private String source;

    @Column(name = "link", length = 2083, nullable = false)
    private String link;
}
