-- V7__alter_report_images_table.sql
-- 신고글이미지 테이블 수정

ALTER TABLE report_images DROP COLUMN IF EXISTS image_key;

-- FK not null로 변경
ALTER TABLE report_images MODIFY report_id BIGINT NOT NULL;