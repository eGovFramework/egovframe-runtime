-- ====================================
-- DROP TABLES (if they exist)
-- ====================================
DROP TABLE IF EXISTS PROPERTY;
COMMIT;

-- ====================================
-- CREATE TABLE: PROPERTY
-- ====================================
CREATE TABLE PROPERTY
(
    PKEY   VARCHAR(20) NOT NULL PRIMARY KEY,
    PVALUE VARCHAR(20) NOT NULL
);
COMMIT;

-- ====================================
-- INSERT TABLE: PROPERTY
-- ====================================
INSERT INTO PROPERTY (PKEY, PVALUE)
VALUES ('egov.test.sample01', 'sample01');
INSERT INTO PROPERTY (PKEY, PVALUE)
VALUES ('egov.test.sample02', 'sample02');
COMMIT;
