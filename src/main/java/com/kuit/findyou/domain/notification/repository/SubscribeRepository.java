package com.kuit.findyou.domain.notification.repository;

import com.kuit.findyou.domain.notification.model.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeRepository extends JpaRepository<Subscribe,Long> {
}
