-- V4: migrate_shelters_to_centers

-- animal_centers 스키마 추가
ALTER TABLE animal_centers ADD COLUMN latitude  DOUBLE NOT NULL;
ALTER TABLE animal_centers ADD COLUMN longitude DOUBLE NOT NULL;

-- animal_centers 스키마 길이 확장
ALTER TABLE animal_centers MODIFY COLUMN jurisdiction VARCHAR(2000) NOT NULL;
ALTER TABLE animal_centers MODIFY COLUMN address VARCHAR(255) NOT NULL;