package org.egovframe.rte.ai.langchain4j.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.langchain4j.etl.EgovLangChain4jMetadata;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import dev.langchain4j.data.segment.TextSegment;

/**
 * JDBC 세그먼트 조회 테스트 — pgvector 표 규약을 HSQLDB 로(스펙 ai/langchain4j-hybrid-retrieval 2항).
 */
class EgovJdbcSegmentLookupTest {

	private static JdbcTemplate jdbc;

	@BeforeAll
	static void schema() {
		JDBCDataSource dataSource = new JDBCDataSource();
		dataSource.setUrl("jdbc:hsqldb:mem:lc4j_segments");
		dataSource.setUser("SA");
		dataSource.setPassword("");
		jdbc = new JdbcTemplate(dataSource);
		jdbc.execute("CREATE TABLE document_embeddings (embedding_id VARCHAR(64) NOT NULL PRIMARY KEY, text LONGVARCHAR, metadata LONGVARCHAR)");
		jdbc.update("INSERT INTO document_embeddings VALUES (?, ?, ?)", "id-1", "전자정부 표준프레임워크 소개",
				"{\"original_id\":\"doc-a\",\"chunk_index\":0,\"id\":\"id-1\"}");
		jdbc.update("INSERT INTO document_embeddings VALUES (?, ?, ?)", "id-2", "페이징 설정 방법", "{\"original_id\":\"doc-a\",\"chunk_index\":1}");
		jdbc.update("INSERT INTO document_embeddings VALUES (?, ?, ?)", "id-3", "다른 문서", null);
	}

	@Test
	@DisplayName("식별자 2건을 본문·메타데이터와 함께 돌려주고 없는 식별자는 생략한다")
	void findsByIds() {
		Map<String, TextSegment> found = EgovJdbcSegmentLookup.of(jdbc).findByIds(List.of("id-1", "id-2", "missing"));

		assertEquals(2, found.size());
		assertEquals("전자정부 표준프레임워크 소개", found.get("id-1").text());
		assertEquals("doc-a", found.get("id-1").metadata().getString(EgovDocumentMetadata.ORIGINAL_ID));
		assertEquals(0, found.get("id-1").metadata().getInteger(EgovLangChain4jMetadata.CHUNK_INDEX));
		assertEquals("id-1", found.get("id-1").metadata().getString(EgovLangChain4jMetadata.SEGMENT_ID));
		assertEquals(1, found.get("id-2").metadata().getInteger(EgovLangChain4jMetadata.CHUNK_INDEX));
		assertFalse(found.containsKey("missing"));
	}

	@Test
	@DisplayName("metadata 가 없는 행은 빈 메타데이터, 빈 식별자 목록은 조회하지 않는다")
	void nullMetadataAndEmptyIds() {
		Map<String, TextSegment> found = EgovJdbcSegmentLookup.of(jdbc).findByIds(List.of("id-3"));

		assertEquals("다른 문서", found.get("id-3").text());
		assertTrue(found.get("id-3").metadata().toMap().isEmpty());
		assertTrue(EgovJdbcSegmentLookup.of(jdbc).findByIds(List.of()).isEmpty());
	}

	@Test
	@DisplayName("SQL 은 식별자를 CAST 한 IN 이고 표·컬럼 이름은 식별자 검증을 거친다")
	void sqlShapeAndIdentifierValidation() {
		String sql = EgovJdbcSegmentLookup.builder(jdbc).table("vec").idColumn("emb_id").textColumn("body").metadataColumn("meta").build().selectSql(2);

		assertEquals("SELECT emb_id, body, meta FROM vec WHERE CAST(emb_id AS VARCHAR(64)) IN (?, ?)", sql);
		assertThrows(IllegalArgumentException.class, () -> EgovJdbcSegmentLookup.builder(jdbc).table("vec; DROP TABLE x").build());
	}
}
