drop table jobhist IF EXISTS;

drop table emp IF EXISTS;

drop table dept IF EXISTS;

CREATE TABLE dept (
    dept_no          NUMERIC(2) NOT NULL,
    dept_name        VARCHAR(14),
    loc             VARCHAR(13),
    CONSTRAINT dept_pk PRIMARY KEY (dept_no),
    CONSTRAINT dept_name_uq UNIQUE (dept_name)
);

CREATE TABLE emp (
    emp_no          NUMERIC(4) NOT NULL,
    emp_name        VARCHAR(10),
    job             VARCHAR(9),
    mgr             NUMERIC(4),
    hire_date       DATE,
    sal             NUMERIC(7),
    comm            NUMERIC(7),
    dept_no         NUMERIC(2),
	CONSTRAINT emp_pk PRIMARY KEY (emp_no),
	CONSTRAINT emp_sal_ck CHECK (sal > 0),
	CONSTRAINT emp_ref_dept_fk FOREIGN KEY (dept_no) REFERENCES dept(dept_no)
);

CREATE TABLE jobhist (
    emp_no           NUMERIC(4) NOT NULL,
    start_date       DATE NOT NULL,
    end_date         DATE,
    job             VARCHAR(9),
    sal             NUMERIC(7),
    comm            NUMERIC(7),
    dept_no          NUMERIC(2),
    chg_desc         VARCHAR(80),
    CONSTRAINT jobhist_pk PRIMARY KEY (emp_no, start_date),
    CONSTRAINT jobhist_ref_emp_fk FOREIGN KEY (emp_no)
        REFERENCES emp(emp_no) ON DELETE CASCADE,
    CONSTRAINT jobhist_ref_dept_fk FOREIGN KEY (dept_no)
        REFERENCES dept (dept_no) ON DELETE SET NULL,
	CONSTRAINT jobhist_date_chk CHECK (start_date <= end_date)
);
