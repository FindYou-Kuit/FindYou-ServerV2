-- V8_create_cache_snapshot_table.sql
-- 레디스 캐시 스냅샷 테이블 작성

CREATE TABLE cache_snapshot (
    cache_key   VARCHAR(128) PRIMARY KEY,
    content  JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);