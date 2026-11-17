-- 문서 해시 저장소(색인 변경 감지) — HSQLDB. 기동 시 자동 실행되지 않는다: 운영자가 명시적으로 적용한다.
-- doc_id 는 추출기가 만드는 안정 식별자(형식 접두·정제 파일명·페이지), content_hash 는 본문 SHA-256 16진(64자).
CREATE TABLE EGOV_AI_DOC_HASH (
    DOC_ID        VARCHAR(512)  NOT NULL,
    CONTENT_HASH  CHAR(64)      NOT NULL,
    UPDATED_AT    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_EGOV_AI_DOC_HASH PRIMARY KEY (DOC_ID)
);
