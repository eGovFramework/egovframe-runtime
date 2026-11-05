package org.egovframe.rte.ai.core.retrieval.lexical;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

/**
 * pg_trgm 백엔드 계약 테스트 — SQL 조립·임계값의 트랜잭션 범위 설정 순서·DDL 미실행·식별자 검증
 * (스펙 ai/hybrid-retrieval 4항). PostgreSQL 없이 JDBC 템플릿·트랜잭션 매니저 스텁으로 검증한다.
 */
class EgovPgTrgmLexicalSearchTest {

	/** 실행된 SQL 과 인자를 기록하는 JdbcTemplate 스텁. */
	private static final class RecordingJdbc extends JdbcTemplate {
		final List<String> sqls = new ArrayList<>();
		final List<Object[]> args = new ArrayList<>();

		@Override
		public <T> T queryForObject(String sql, Class<T> requiredType, Object... params) {
			sqls.add(sql);
			args.add(params);
			return requiredType.cast("0.3");
		}

		@Override
		public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
			sqls.add(sql);
			args.add(params);
			@SuppressWarnings("unchecked")
			List<T> ids = (List<T>) List.of("doc-3", "doc-1");
			return ids;
		}
	}

	/** 트랜잭션 시작·커밋 횟수를 세는 매니저 스텁. */
	private static final class CountingTransactionManager implements PlatformTransactionManager {
		int begun;
		int committed;
		int rolledBack;

		@Override
		public TransactionStatus getTransaction(TransactionDefinition definition) {
			begun++;
			return new SimpleTransactionStatus(true);
		}

		@Override
		public void commit(TransactionStatus status) {
			committed++;
		}

		@Override
		public void rollback(TransactionStatus status) {
			rolledBack++;
		}
	}

	@Test
	@DisplayName("검색 SQL 은 %> 필터·word_similarity 내림차순·metadata id 를 쓴다")
	void selectSql() {
		assertEquals("SELECT metadata::jsonb ->> 'id' AS doc_id FROM document_embeddings"
				+ " WHERE text %> ? ORDER BY word_similarity(?, text) DESC LIMIT ?",
				EgovPgTrgmLexicalSearch.selectSql("document_embeddings", "text", "metadata"));
		assertEquals("SELECT set_config('pg_trgm.word_similarity_threshold', ?, true)", EgovPgTrgmLexicalSearch.SET_THRESHOLD_SQL);
	}

	@Test
	@DisplayName("임계값을 같은 트랜잭션에서 먼저 설정한 뒤 검색하고, DDL 은 실행하지 않는다")
	void thresholdIsSetInTransactionBeforeQuery() {
		RecordingJdbc jdbc = new RecordingJdbc();
		CountingTransactionManager tm = new CountingTransactionManager();
		EgovPgTrgmLexicalSearch search = new EgovPgTrgmLexicalSearch(jdbc, tm, "document_embeddings", 0.30);

		List<String> ids = search.search("전자정부 페이징", 3);

		assertEquals(List.of("doc-3", "doc-1"), ids);
		assertEquals(2, jdbc.sqls.size());
		assertEquals(EgovPgTrgmLexicalSearch.SET_THRESHOLD_SQL, jdbc.sqls.get(0));
		assertArrayEquals(new Object[] { "0.3" }, jdbc.args.get(0));
		assertEquals(search.sql(), jdbc.sqls.get(1));
		assertArrayEquals(new Object[] { "전자정부 페이징", "전자정부 페이징", 3 }, jdbc.args.get(1));
		assertEquals(1, tm.begun);
		assertEquals(1, tm.committed);
		assertEquals(0, tm.rolledBack);
		for (String sql : jdbc.sqls) {
			assertTrue(!sql.toUpperCase().contains("CREATE "), sql);
		}
	}

	@Test
	@DisplayName("빈 질의·상한 0 은 DB 를 호출하지 않는다")
	void blankQueryOrZeroLimitSkipsDb() {
		RecordingJdbc jdbc = new RecordingJdbc();
		CountingTransactionManager tm = new CountingTransactionManager();
		EgovPgTrgmLexicalSearch search = new EgovPgTrgmLexicalSearch(jdbc, tm, "document_embeddings", 0.30);
		assertEquals(List.of(), search.search("   ", 3));
		assertEquals(List.of(), search.search("질의", 0));
		assertTrue(jdbc.sqls.isEmpty());
		assertEquals(0, tm.begun);
	}

	@Test
	@DisplayName("식별자가 아닌 테이블·컬럼명과 범위 밖 임계값은 생성 시 거부된다")
	void validation() {
		RecordingJdbc jdbc = new RecordingJdbc();
		CountingTransactionManager tm = new CountingTransactionManager();
		assertThrows(IllegalArgumentException.class, () -> new EgovPgTrgmLexicalSearch(jdbc, tm, "docs; DROP TABLE x", 0.3));
		assertThrows(IllegalArgumentException.class, () -> EgovPgTrgmLexicalSearch.selectSql("t", "text)", "metadata"));
		assertThrows(IllegalArgumentException.class, () -> new EgovPgTrgmLexicalSearch(jdbc, tm, "docs", 1.5));
	}
}
