-- 채번 테이블(EgovTableIdGnrServiceImpl) — ANSI 기준 DDL. PostgreSQL·MySQL·Oracle 등은 이 형태를 방언에 맞게 손질해 적용한다
-- (예: Oracle 은 VARCHAR2·NUMBER(30), MySQL 은 DECIMAL(30,0)). 기동 시 자동 실행되지 않는다.
-- 기본 구성(table=ids, tableNameFieldName=table_name, nextIdFieldName=next_id)에 맞춘 이름이다.
-- 빈 설정(table·tableNameFieldName·nextIdFieldName)으로 이름을 바꾸면 이 스크립트도 같이 바꾼다.
-- 행(table_name 별 next_id)은 첫 채번 때 없으면 서비스가 INSERT 하므로 미리 넣지 않아도 된다.
CREATE TABLE ids (
    table_name  VARCHAR(16)  NOT NULL,
    next_id     DECIMAL(30)  NOT NULL,
    CONSTRAINT PK_IDS PRIMARY KEY (table_name)
);
