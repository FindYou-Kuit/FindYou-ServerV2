package com.kuit.findyou.domain.notification.repository;

import com.kuit.findyou.domain.notification.model.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
}
