package com.kuit.findyou.global.common.util;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseCleaner {

    private final EntityManager em;

    private static final List<String> EXCLUDED_TABLES = List.of(
            "flyway_schema_history" // Flyway 사용 시 기본 제외
    );

    public DatabaseCleaner(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void execute() {
        // 현재 DB 스키마명 조회
        String schema = (String) em.createNativeQuery("SELECT DATABASE()").getSingleResult();

        // 현재 스키마의 모든 물리 테이블명 조회
        @SuppressWarnings("unchecked")
        List<String> tableNames = em.createNativeQuery("""
                SELECT TABLE_NAME
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = :schema
                  AND TABLE_TYPE = 'BASE TABLE'
                """)
                .setParameter("schema", schema)
                .getResultList();

        // 제외 목록 제거
        List<String> targets = new ArrayList<>();
        for (String t : tableNames) {
            if (!EXCLUDED_TABLES.contains(t)) {
                targets.add(t);
            }
        }

        // FK 체크 끄기
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        try {
            // 모든 테이블 TRUNCATE (스키마.테이블로 지정, 백틱으로 이스케이프)
            for (String table : targets) {
                String sql = "TRUNCATE TABLE `" + schema + "`.`" + table + "`";
                em.createNativeQuery(sql).executeUpdate();
            }
        } finally {
            // FK 체크 다시 켜기 (예외가 나도 꼭 실행)
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        }
    }
}