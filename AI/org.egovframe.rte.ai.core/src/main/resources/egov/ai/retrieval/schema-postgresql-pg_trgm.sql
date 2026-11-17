-- 하이브리드 검색 어휘 채널(pg_trgm) 준비 — PostgreSQL. 기동 시 자동 실행되지 않는다: DBA 가 명시적으로 적용한다.
-- 대상 테이블·컬럼은 벡터 저장소가 만든 것(LangChain4j PgVectorEmbeddingStore 기본: document_embeddings.text,
-- Spring AI PgVectorStore 기본: vector_store.content) — 자기 구성에 맞게 이름을 바꿔 적용한다.
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_doc_emb_trgm ON document_embeddings USING gin (text gin_trgm_ops);
