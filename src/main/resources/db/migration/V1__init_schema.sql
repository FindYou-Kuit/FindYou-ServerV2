-- V1__init_schema.sql
-- 초기 데이터베이스 스키마 생성

-- 1. 사용자 테이블
CREATE TABLE users
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(50),
    profile_image_url    VARCHAR(2083),
    kakao_id             BIGINT,
    role                 VARCHAR(20) NOT NULL,
    receive_notification CHAR(1)     NOT NULL DEFAULT 'N',
    device_id            VARCHAR(100) UNIQUE,
    status               CHAR(1)     NOT NULL DEFAULT 'Y',
    created_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 시도 테이블
CREATE TABLE sidos
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    status     CHAR(1)     NOT NULL DEFAULT 'Y',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. 시군구 테이블
CREATE TABLE sigungus
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    sido_id    BIGINT      NOT NULL,
    status     CHAR(1)     NOT NULL DEFAULT 'Y',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sido_id) REFERENCES sidos (id)
);

-- 4. 품종 테이블
CREATE TABLE breeds
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    breed_name VARCHAR(100) NOT NULL,
    species    VARCHAR(20)  NOT NULL,
    status     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 5. 동물보호센터 테이블
CREATE TABLE animal_centers
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    jurisdiction VARCHAR(100) NOT NULL,
    center_name  VARCHAR(70)  NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    address      VARCHAR(255) NOT NULL,
    status       CHAR(1)      NOT NULL DEFAULT 'Y',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 6. 동물보호과 테이블
CREATE TABLE animal_departments
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization VARCHAR(100) NOT NULL,
    department   VARCHAR(70)  NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    status       CHAR(1)      NOT NULL DEFAULT 'Y',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 7. 동물보호소 테이블
CREATE TABLE animal_shelters
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    address      VARCHAR(100)  NOT NULL,
    jurisdiction VARCHAR(2000) NOT NULL,
    phone_number VARCHAR(20)   NOT NULL,
    shelter_name VARCHAR(70)   NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    status       CHAR(1)       NOT NULL DEFAULT 'Y',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 8. 봉사활동 테이블
CREATE TABLE volunteer_works
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    recruitment_start_at DATE,
    recruitment_end_at   DATE,
    place                VARCHAR(255),
    volunteer_start_at   DATE,
    volunteer_end_at     DATE,
    volunteer_time       VARCHAR(50),
    status               CHAR(1)  NOT NULL DEFAULT 'Y',
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 9. 신고글 기본 테이블 (상속 구조)
CREATE TABLE reports
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    breed      VARCHAR(20)  NOT NULL,
    species    VARCHAR(100) NOT NULL,
    tag        VARCHAR(20)  NOT NULL,
    date       DATE         NOT NULL,
    address    VARCHAR(200) NOT NULL,
    latitude   DECIMAL(9, 6),
    longitude  DECIMAL(9, 6),
    user_id    BIGINT,
    dtype      VARCHAR(31)  NOT NULL,
    status     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 10. 실종신고 테이블
CREATE TABLE missing_reports
(
    id            BIGINT PRIMARY KEY,
    sex           VARCHAR(10)  NOT NULL,
    rfid          VARCHAR(30),
    age           VARCHAR(10)  NOT NULL,
    weight        VARCHAR(20)  NOT NULL,
    fur_color     VARCHAR(100)  NOT NULL,
    significant   VARCHAR(255) NOT NULL,
    reporter_name VARCHAR(20),
    reporter_tel  VARCHAR(20),
    landmark      VARCHAR(255) NOT NULL,
    FOREIGN KEY (id) REFERENCES reports (id)
);

-- 11. 보호신고 테이블
CREATE TABLE protecting_reports
(
    id                BIGINT PRIMARY KEY,
    sex               CHAR(1)      NOT NULL,
    age               VARCHAR(10)  NOT NULL,
    weight            VARCHAR(10)  NOT NULL,
    fur_color         VARCHAR(100)  NOT NULL,
    neutering         CHAR(1)      NOT NULL,
    significant       VARCHAR(255) NOT NULL,
    found_location    VARCHAR(100) NOT NULL,
    notice_number     VARCHAR(30)  NOT NULL,
    notice_start_date DATE         NOT NULL,
    notice_end_date   DATE         NOT NULL,
    care_name         VARCHAR(50)  NOT NULL,
    care_tel          VARCHAR(14)  NOT NULL,
    authority         VARCHAR(50)  NOT NULL,
    FOREIGN KEY (id) REFERENCES reports (id)
);

-- 12. 목격신고 테이블
CREATE TABLE witness_reports
(
    id            BIGINT PRIMARY KEY,
    fur_color     VARCHAR(100) NOT NULL,
    significant   VARCHAR(255) NOT NULL,
    reporter_name VARCHAR(50),
    landmark      VARCHAR(255) NOT NULL,
    FOREIGN KEY (id) REFERENCES reports (id)
);

-- 13. 신고글 이미지 테이블
CREATE TABLE report_images
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_url  VARCHAR(2083)         NOT NULL,
    uuid       VARCHAR(255) NOT NULL,
    report_id  BIGINT,
    status     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports (id)
);

-- 14. 관심글 테이블
CREATE TABLE interest_reports
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT   NOT NULL,
    report_id  BIGINT   NOT NULL,
    status     CHAR(1)  NOT NULL DEFAULT 'Y',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (report_id) REFERENCES reports (id)
);

-- 15. 최근 본 글 테이블
CREATE TABLE viewed_reports
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT   NOT NULL,
    report_id  BIGINT   NOT NULL,
    status     CHAR(1)  NOT NULL DEFAULT 'Y',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (report_id) REFERENCES reports (id)
);

-- 16. 키워드 테이블
CREATE TABLE keywords
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(30) NOT NULL,
    status     CHAR(1)     NOT NULL DEFAULT 'Y',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 17. 구독 테이블
CREATE TABLE subscribes
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword_id BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    status     CHAR(1)  NOT NULL DEFAULT 'Y',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (keyword_id) REFERENCES keywords (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 18. FCM 토큰 테이블
CREATE TABLE fcm_tokens
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    fcm_token  VARCHAR(300) NOT NULL,
    user_id    BIGINT       NOT NULL,
    status     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 19. 알림 내역 테이블
CREATE TABLE notification_histories
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    is_viewed  CHAR(1)  NOT NULL,
    user_id    BIGINT   NOT NULL,
    report_id  BIGINT   NOT NULL,
    status     CHAR(1)  NOT NULL DEFAULT 'Y',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (report_id) REFERENCES reports (id)
);

-- 20. 추천 뉴스 테이블
CREATE TABLE recommended_news
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(255)  NOT NULL,
    url        VARCHAR(2083) NOT NULL,
    uploader   VARCHAR(255)  NOT NULL,
    status     CHAR(1)       NOT NULL DEFAULT 'Y',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 21. 추천 비디오 테이블
CREATE TABLE recommended_videos
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(255)  NOT NULL,
    url        VARCHAR(2083) NOT NULL,
    uploader   VARCHAR(255)  NOT NULL,
    status     CHAR(1)       NOT NULL DEFAULT 'Y',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 성능 최적화를 위한 추가 인덱스 (Hibernate가 자동 생성하지 않는 것들)
CREATE INDEX idx_users_kakao_id ON users (kakao_id);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_reports_tag ON reports (tag);
