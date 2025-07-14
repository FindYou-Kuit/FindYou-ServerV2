-- src/test/resources/data/sql/report-controller-test-data.sql

-- 사용자 삽입
INSERT INTO users (id, name, profile_image_url, kakao_id, role, status, created_at, updated_at)
VALUES (1, '홍길동', 'http://example.com/profile.png', 123456789, 'USER', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 보호 글 삽입
INSERT INTO reports (id, breed, species, tag, date, address, user_id, dtype, status, created_at, updated_at)
VALUES (1, '믹스견', '개', 'PROTECTING', CURRENT_DATE, '서울', 1, 'PROTECTING', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO protecting_reports (id, sex, age, weight, fur_color, neutering, significant, found_location,
                                notice_number, notice_start_date, notice_end_date, care_name, care_tel, authority, latitude, longitude)
VALUES (1, 'M', '2살', '5kg', '갈색', 'Y', '절뚝거림', '홍대',
        'NOTICE123', CURRENT_DATE, DATEADD('DAY', 10, CURRENT_DATE), '광진보호소', '02', '관청', 37.000000, 127.000000);

INSERT INTO report_images (id, report_id, image_url, uuid, status, created_at, updated_at)
VALUES (1, 1, 'https://img.com/1.png', 'uuid-1', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 실종 글 삽입
INSERT INTO reports (id, breed, species, tag, date, address, user_id, dtype, status, created_at, updated_at)
VALUES (2, '포메라니안', '개', 'MISSING', DATE '2024-10-05', '서울시 강남구', 1, 'MISSING', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO missing_reports (id, sex, rfid, age, weight, fur_color, significant,
                             reporter_name, reporter_tel, landmark, latitude, longitude)
VALUES (2, 'F', 'RF12345', '3살', '3kg', '흰색', '눈 주변 갈색 털',
        '이슬기', '010-1111-2222', '강남역 10번 출구', 37.501000, 127.025000);

INSERT INTO report_images (id, report_id, image_url, uuid, status, created_at, updated_at)
VALUES (2, 2, 'https://img.com/missing.png', 'uuid-m', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 목격 글 삽입
INSERT INTO reports (id, breed, species, tag, date, address, user_id, dtype, status, created_at, updated_at)
VALUES (3, '진돗개', '개', 'WITNESS', DATE '2024-08-10', '부산시 해운대구', 1, 'WITNESS', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO witness_reports (id, fur_color, significant, reporter_name, landmark, latitude, longitude)
VALUES (3, '하얀 털', '목줄 없음', '신성훈', '해변가', 35.158000, 129.160000);

INSERT INTO report_images (id, report_id, image_url, uuid, status, created_at, updated_at)
VALUES (3, 3, 'https://img.com/witness.png', 'uuid-w', 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 관심 글 설정 (사용자 1번이 각 글에 관심 설정)
INSERT INTO interest_reports (id, user_id, report_id, status, created_at, updated_at)
VALUES (1, 1, 1, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 1, 2, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 1, 3, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
