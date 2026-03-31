-- ====================================
-- idgnr 테스트용 데이터베이스 스크립트 (통합)
-- Table/Sequence 테스트 공통 사용
-- ====================================

-- ------------------------------------
-- 1. 기존 객체 제거
-- ------------------------------------
DROP SEQUENCE idstest IF EXISTS CASCADE;
DROP TABLE idttest IF EXISTS CASCADE;
DROP TABLE ids IF EXISTS CASCADE;
COMMIT;

-- ------------------------------------
-- 2. 테이블 생성
-- ------------------------------------
CREATE TABLE idttest
(
    table_name varchar(16) NOT NULL,
    next_id    DECIMAL(30) NOT NULL,
    PRIMARY KEY (table_name)
);

CREATE TABLE ids
(
    table_name varchar(16) NOT NULL,
    next_id    DECIMAL(30) NOT NULL,
    PRIMARY KEY (table_name)
);
COMMIT;

-- ------------------------------------
-- 3. 시퀀스 생성
-- ------------------------------------
CREATE SEQUENCE idstest START WITH 1 INCREMENT BY 1;
COMMIT;

-- ------------------------------------
-- 4. 초기 데이터
-- ------------------------------------
INSERT INTO idttest
VALUES ('id', '0');
INSERT INTO ids
VALUES ('id', '0');
COMMIT;
