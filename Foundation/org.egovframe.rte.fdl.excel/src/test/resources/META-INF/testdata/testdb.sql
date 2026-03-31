-- ====================================
-- DROP TABLES (if they exist)
-- ====================================
DROP TABLE dept IF EXISTS CASCADE;
DROP TABLE emp IF EXISTS CASCADE;
DROP TABLE jobhist IF EXISTS CASCADE;
DROP TABLE zip IF EXISTS CASCADE;
COMMIT;

-- ====================================
-- CREATE TABLE: dept
-- ====================================
CREATE TABLE dept
(
    dept_no   NUMERIC(2) NOT NULL,
    dept_name VARCHAR(14),
    loc       VARCHAR(13)
);
COMMIT;

-- ====================================
-- CREATE TABLE: emp
-- ====================================
CREATE TABLE emp
(
    emp_no    NUMERIC(4) NOT NULL,
    emp_name  VARCHAR(10),
    job       VARCHAR(9),
    mgr       NUMERIC(4),
    hire_date DATE,
    sal       NUMERIC(7, 2),
    comm      NUMERIC(7, 2),
    dept_no   NUMERIC(2)
);
COMMIT;

-- ====================================
-- CREATE TABLE: jobhist
-- ====================================
CREATE TABLE jobhist
(
    emp_no     NUMERIC(4) NOT NULL,
    start_date DATE       NOT NULL,
    end_date   DATE,
    job        VARCHAR(9),
    sal        NUMERIC(7, 2),
    comm       NUMERIC(7, 2),
    dept_no    NUMERIC(2),
    chg_desc   VARCHAR(80)
);
COMMIT;

-- ====================================
-- CREATE TABLE: zip
-- ====================================
CREATE TABLE zip
(
    zip_no  VARCHAR(6)    NOT NULL,
    ser_no  NUMERIC(7, 0) NOT NULL,
    sido_nm VARCHAR(20),
    cgg_nm  VARCHAR(20),
    umd_nm  VARCHAR(20),
    bd_nm   VARCHAR(50),
    jibun   VARCHAR(20),
    reg_id  VARCHAR(20)
);
COMMIT;

-- ====================================
-- ADD PRIMARY KEYS
-- ====================================
ALTER TABLE dept
    ADD CONSTRAINT dept_pk PRIMARY KEY (dept_no);
ALTER TABLE emp
    ADD CONSTRAINT emp_pk PRIMARY KEY (emp_no);
ALTER TABLE jobhist
    ADD CONSTRAINT jobhist_pk PRIMARY KEY (emp_no, start_date);
ALTER TABLE zip
    ADD CONSTRAINT zip_pk PRIMARY KEY (zip_no, ser_no);

-- ====================================
-- ADD UNIQUE
-- ====================================
ALTER TABLE dept
    ADD CONSTRAINT dept_name_uq UNIQUE (dept_name);

-- ====================================
-- ADD FOREIGN KEYS
-- ====================================
ALTER TABLE emp
    ADD CONSTRAINT emp_ref_dept_fk FOREIGN KEY (dept_no) REFERENCES dept (dept_no);
ALTER TABLE jobhist
    ADD CONSTRAINT jobhist_ref_emp_fk FOREIGN KEY (emp_no) REFERENCES emp (emp_no);
ALTER TABLE jobhist
    ADD CONSTRAINT jobhist_ref_dept_fk FOREIGN KEY (dept_no) REFERENCES dept (dept_no);

-- ====================================
-- ADD CHECK
-- ====================================
ALTER TABLE emp
    ADD CONSTRAINT emp_sal_ck CHECK (sal > 0);
ALTER TABLE jobhist
    ADD CONSTRAINT jobhist_date_chk CHECK (start_date <= end_date);
