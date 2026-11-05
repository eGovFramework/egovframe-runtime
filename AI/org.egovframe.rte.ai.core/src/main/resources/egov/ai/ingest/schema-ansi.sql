-- 문서 해시 저장소(색인 변경 감지) — ANSI 기준 DDL. PostgreSQL·MySQL·Oracle 등은 이 형태를 방언에 맞게 손질해 적용한다
-- (예: Oracle 은 VARCHAR2·SYSTIMESTAMP). 기동 시 자동 실행되지 않는다.
CREATE TABLE EGOV_AI_DOC_HASH (
    DOC_ID        VARCHAR(512)  NOT NULL,
    CONTENT_HASH  CHAR(64)      NOT NULL,
    UPDATED_AT    TIMESTAMP     NOT NULL,
    CONSTRAINT PK_EGOV_AI_DOC_HASH PRIMARY KEY (DOC_ID)
);
