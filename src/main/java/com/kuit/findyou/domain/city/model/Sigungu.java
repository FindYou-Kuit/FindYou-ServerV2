package com.kuit.findyou.domain.city.model;

import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "sigungu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("status = 'Y'")
public class Sigungu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sido_id", nullable = false)
    private Sido sido;

    //==생성 메서드==// -> 생성자 말고 생성 메서드를 별도로 만든 형태
    public static Sigungu createSigungu(String name, Sido sido) {
        Sigungu sigungu = new Sigungu();
        sigungu.name = name;
        sigungu.setSido(sido);
        return sigungu;
    }

    // User 에 대한 연관 관계 편의 메서드
    private void setSido(Sido sido) {
        this.sido = sido;
        sido.addSigungu(this);
    }
}
