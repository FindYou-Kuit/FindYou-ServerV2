package com.kuit.findyou.domain.notification.repository;

import com.kuit.findyou.domain.notification.model.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
}
