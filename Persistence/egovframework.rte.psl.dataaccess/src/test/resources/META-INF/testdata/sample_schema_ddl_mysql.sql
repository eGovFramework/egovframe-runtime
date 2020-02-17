DROP TABLE IF EXISTS JOBHIST;

DROP TABLE IF EXISTS EMP;

DROP TABLE IF EXISTS DEPT;

CREATE TABLE DEPT (
    dept_no          NUMERIC(2) NOT NULL,
    dept_name        VARCHAR(14),
    loc             VARCHAR(13),
    CONSTRAINT dept_pk PRIMARY KEY (dept_no),
    CONSTRAINT dept_name_uq UNIQUE (dept_name)
);

CREATE TABLE EMP (
    emp_no          NUMERIC(4) NOT NULL,
    emp_name        VARCHAR(10),
    job             VARCHAR(9),
    mgr             NUMERIC(4),
    hire_date       DATE,
    sal             NUMERIC(5), /* NUMERIC(7,2), - mysql 은 소숫점에 .00 이 자동으로 들어감 */
    comm            NUMERIC(5), /* NUMERIC(7,2), */
    dept_no         NUMERIC(2),
	CONSTRAINT emp_pk PRIMARY KEY (emp_no),
	CONSTRAINT emp_sal_ck CHECK (sal > 0),
	CONSTRAINT emp_ref_dept_fk FOREIGN KEY (dept_no) REFERENCES DEPT(dept_no)
);

CREATE TABLE JOBHIST (
    emp_no           NUMERIC(4) NOT NULL,
    start_date       DATE NOT NULL,
    end_date         DATE,
    job             VARCHAR(9),
    sal             NUMERIC(5), /* NUMERIC(7,2), */
    comm            NUMERIC(5), /* NUMERIC(7,2), */
    dept_no          NUMERIC(2),
    chg_desc         VARCHAR(80),
    CONSTRAINT jobhist_pk PRIMARY KEY (emp_no, start_date),
    CONSTRAINT jobhist_ref_emp_fk FOREIGN KEY (emp_no)
        REFERENCES EMP(emp_no) ON DELETE CASCADE,
    CONSTRAINT jobhist_ref_dept_fk FOREIGN KEY (dept_no)
        REFERENCES DEPT(dept_no) ON DELETE SET NULL,
	CONSTRAINT jobhist_date_chk CHECK (start_date <= end_date)
);

DROP TABLE IF EXISTS EMP_COPY;

CREATE TABLE EMP_COPY (
    emp_no          NUMERIC(4) NOT NULL,
    emp_name        VARCHAR(10),
    job             VARCHAR(9),
    mgr             NUMERIC(4),
    hire_date       DATE,
    sal             NUMERIC(5), /* NUMERIC(7,2), */
    comm            NUMERIC(5), /* NUMERIC(7,2), */
    dept_no         NUMERIC(2)
);