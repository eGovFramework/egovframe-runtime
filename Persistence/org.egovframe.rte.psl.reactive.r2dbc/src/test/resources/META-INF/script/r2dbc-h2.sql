-- ====================================
-- DROP TABLES (if they exist)
-- ====================================
DROP TABLE sample IF EXISTS;
COMMIT;

-- ====================================
-- CREATE TABLE: sample
-- ====================================
CREATE TABLE sample
(
    id          int PRIMARY KEY auto_increment,
    sample_id   VARCHAR(16) NOT NULL,
    name        VARCHAR(50),
    description VARCHAR(100),
    use_yn      CHAR(1),
    reg_user    VARCHAR(10)
);
COMMIT;
