-- V3__alter_missing_report_table.sql
-- 실종신고글 테이블 수정

ALTER TABLE missing_reports DROP COLUMN weight;

-- make columns nullable
ALTER TABLE missing_reports
    MODIFY sex VARCHAR(20) NULL,
    MODIFY rfid VARCHAR(30) NULL,
    MODIFY age VARCHAR(10) NULL,
    MODIFY fur_color VARCHAR(100) NULL,
    MODIFY significant VARCHAR(255) NULL,
    MODIFY reporter_name VARCHAR(20) NULL,
    MODIFY reporter_tel VARCHAR(20) NULL,
    MODIFY landmark VARCHAR(255) NULL;