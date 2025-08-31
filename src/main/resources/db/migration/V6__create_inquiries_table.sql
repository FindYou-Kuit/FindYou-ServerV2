-- V6__create_inquiries_table.sql
-- 문의사항 테이블 생성

-- 문의사항 테이블
CREATE TABLE inquiries
(
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(300)    NOT NULL,
    category        VARCHAR(1024)   NOT NULL,
    content         TEXT            NOT NULL,
    user_id         BIGINT          NOT NULL,
    status          CHAR(1)         NOT NULL DEFAULT 'Y',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);