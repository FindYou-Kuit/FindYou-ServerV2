package com.kuit.findyou.domain.notification.model;

import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "fcm_tokens")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id", nullable = false)
    private Long id;

    @Column(name = "fcm_token", nullable = false, length = 100)
    private String fcmToken;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static FcmToken createFcmToken(User user, String tokenValue) {
        FcmToken token = FcmToken.builder()
                .fcmToken(tokenValue)
                .user(user)
                .build();
        token.setUser(user);
        return token;
    }

    private void setUser(User user) {
        this.user = user;
        user.setFcmToken(this);
    }
}
