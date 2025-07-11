package com.kuit.findyou.domain.subscribe.repository;

import com.kuit.findyou.domain.subscribe.model.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeRepository extends JpaRepository<Subscribe,Long> {
}
