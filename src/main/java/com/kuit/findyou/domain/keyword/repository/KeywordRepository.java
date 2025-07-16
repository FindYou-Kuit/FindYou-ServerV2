package com.kuit.findyou.domain.keyword.repository;

import com.kuit.findyou.domain.keyword.model.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
}
