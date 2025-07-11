package com.kuit.findyou.domain.subscribe.model;

import com.kuit.findyou.domain.keyword.model.Keyword;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "subscribes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE subscribes SET status = 'N' WHERE subscribe_id = ?")
@SQLRestriction(value = "status = 'Y'")
public class Subscribe extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscribe_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static Subscribe createSubscribe(User user, Keyword keyword) {
        Subscribe subscribe = new Subscribe();
        subscribe.setUser(user);
        subscribe.setKeyword(keyword);
        return subscribe;
    }

    private void setUser(User user) {
        this.user = user;
        user.addSubscribe(this);
    }

    private void setKeyword(Keyword keyword) {
        this.keyword = keyword;
        keyword.addSubscribe(this);
    }
}
