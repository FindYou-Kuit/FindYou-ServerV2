package com.kuit.findyou.domain.notification.repository;

import com.kuit.findyou.domain.notification.model.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
}
