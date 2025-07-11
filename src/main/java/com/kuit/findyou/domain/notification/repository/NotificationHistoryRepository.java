package com.kuit.findyou.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.management.Notification;

public interface NotificationHistoryRepository extends JpaRepository<Notification, Long> {
}
