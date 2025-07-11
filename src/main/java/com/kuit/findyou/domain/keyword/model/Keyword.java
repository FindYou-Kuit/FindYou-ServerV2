package com.kuit.findyou.domain.keyword.model;

import com.kuit.findyou.domain.subscribe.model.Subscribe;
import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE keywords SET status = 'N' WHERE keywoed_id = ?")
@SQLRestriction("status = 'Y'")
public class Keyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @OneToMany(mappedBy = "keyword", orphanRemoval = true)
    @Builder.Default
    private List<Subscribe> subscribes = new ArrayList<>();

    public void addSubscribe(Subscribe subscribe) {
        subscribes.add(subscribe);
    }



}
