package com.kuit.findyou.domain.notification.model;

import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscribes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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
