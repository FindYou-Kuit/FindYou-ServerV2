-- V4: migrate_shelters_to_centers

-- 1) animal_centers 스키마 확장
ALTER TABLE animal_centers ADD COLUMN latitude  DOUBLE NULL;
ALTER TABLE animal_centers ADD COLUMN longitude DOUBLE NULL;
ALTER TABLE animal_centers MODIFY COLUMN jurisdiction VARCHAR(2000) NOT NULL;

-- 2) 데이터 이전 (shelters -> centers)
INSERT INTO animal_centers
(id, jurisdiction, center_name, phone_number, address, latitude, longitude, status, created_at, updated_at)
SELECT
    s.id, s.jurisdiction, s.shelter_name, s.phone_number, s.address,
    s.latitude, s.longitude, s.status, s.created_at, s.updated_at
FROM animal_shelters s;

-- 3) 기존에 있던 row와 동기화
UPDATE animal_centers c
    JOIN animal_shelters s ON s.id = c.id
    SET c.jurisdiction = s.jurisdiction,
        c.center_name  = s.shelter_name,
        c.phone_number = s.phone_number,
        c.address      = s.address,
        c.latitude     = s.latitude,
        c.longitude    = s.longitude,
        c.status       = s.status,
        c.updated_at   = s.updated_at;