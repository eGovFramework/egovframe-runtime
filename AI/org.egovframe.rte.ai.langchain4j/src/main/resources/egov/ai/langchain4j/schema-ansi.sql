-- LangChain4j 채팅 메모리 저장소(EgovJdbcChatMemoryStore) — ANSI 기준 DDL. 메모리 id 당 한 행에 LangChain4j 직렬화기의
-- JSON(MESSAGES)을 저장한다. PostgreSQL 은 CLOB 대신 TEXT, Oracle 은 VARCHAR2·SYSTIMESTAMP 등 방언에 맞게 손질해 적용한다.
-- 기동 시 자동 실행되지 않는다.
CREATE TABLE EGOV_AI_CHAT_MEMORY (
    MEMORY_ID   VARCHAR(100)  NOT NULL,
    MESSAGES    CLOB          NOT NULL,
    UPDATED_AT  TIMESTAMP     NOT NULL,
    CONSTRAINT PK_EGOV_AI_CHAT_MEMORY PRIMARY KEY (MEMORY_ID)
);
