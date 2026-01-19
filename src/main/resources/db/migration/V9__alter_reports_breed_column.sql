-- V9__alter_reports_breed_column.sql
-- reports 테이블의 breed 컬럼 길이를 20에서 50으로 변경

ALTER TABLE reports MODIFY COLUMN breed VARCHAR(50) NOT NULL;
