package com.kuit.findyou.domain.fcmToken.repository;

import com.kuit.findyou.domain.fcmToken.model.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
}
