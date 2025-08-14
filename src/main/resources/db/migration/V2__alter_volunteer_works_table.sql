-- V2__alter_volunteer_works_table.sql
-- 봉사활동 테이블 수정

-- 더 이상 쓰지 않는 컬럼 정리
ALTER TABLE volunteer_works DROP COLUMN place;
ALTER TABLE volunteer_works DROP COLUMN volunteer_time;

--  컬럼 추가
ALTER TABLE volunteer_works ADD COLUMN institution VARCHAR(70);
ALTER TABLE volunteer_works ADD COLUMN address VARCHAR(255);
ALTER TABLE volunteer_works ADD COLUMN volunteer_start_time VARCHAR(10);
ALTER TABLE volunteer_works ADD COLUMN volunteer_end_time VARCHAR(10);
ALTER TABLE volunteer_works ADD COLUMN web_link VARCHAR(2083);

-- 날짜 컬럼 이름 변경(_at -> _date)
ALTER TABLE volunteer_works RENAME COLUMN recruitment_start_at TO recruitment_start_date;
ALTER TABLE volunteer_works RENAME COLUMN recruitment_end_at   TO recruitment_end_date;
ALTER TABLE volunteer_works RENAME COLUMN volunteer_start_at   TO volunteer_start_date;
ALTER TABLE volunteer_works RENAME COLUMN volunteer_end_at     TO volunteer_end_date;

-- 칼럼 삭제
ALTER TABLE volunteer_works DROP COLUMN volunteer_start_date;
ALTER TABLE volunteer_works DROP COLUMN volunteer_end_date;
ALTER TABLE volunteer_works DROP COLUMN volunteer_start_time;
ALTER TABLE volunteer_works DROP COLUMN volunteer_end_time;

-- 칼럼 추가
ALTER TABLE volunteer_works ADD COLUMN register_number VARCHAR(20);
ALTER TABLE volunteer_works ADD COLUMN volunteer_start_at DATETIME;
ALTER TABLE volunteer_works ADD COLUMN volunteer_end_at DATETIME;

