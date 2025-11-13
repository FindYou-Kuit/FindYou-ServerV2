package com.kuit.findyou.domain.notification.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Keyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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
