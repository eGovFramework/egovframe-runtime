package org.egovframe.rte.ai.core.ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/**
 * 변경 감지 계약 테스트 — SHA-256 해시, 메모리·JDBC 저장소 왕복(HSQLDB + 동봉 스키마 스크립트), 테이블 부재 예외, 변경 판정
 * (스펙 ai/ingest-change-detection).
 */
class EgovDocumentHashStoreTest {

	private static JdbcTemplate hsqldb(String name, boolean withSchema) throws Exception {
		JDBCDataSource dataSource = new JDBCDataSource();
		dataSource.setUrl("jdbc:hsqldb:mem:" + name);
		dataSource.setUser("SA");
		dataSource.setPassword("");
		if (withSchema) {
			try (Connection connection = dataSource.getConnection()) {
				ScriptUtils.executeSqlScript(connection, new ClassPathResource("egov/ai/ingest/schema-hsqldb.sql"));
			}
		}
		return new JdbcTemplate(dataSource);
	}

	@Test
	@DisplayName("SHA-256 16진 64자 — 결정적이고 한 글자 차이에 달라지며 null 은 거부된다")
	void contentHash() {
		String hash = EgovContentHash.sha256Hex("전자정부 표준프레임워크");
		assertEquals(64, hash.length());
		assertEquals(hash, EgovContentHash.sha256Hex("전자정부 표준프레임워크"));
		assertNotEquals(hash, EgovContentHash.sha256Hex("전자정부 표준프레임워크 "));
		assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", EgovContentHash.sha256Hex(""));
		assertThrows(NullPointerException.class, () -> EgovContentHash.sha256Hex(null));
	}

	@Test
	@DisplayName("메모리 저장소 — 저장·조회·삭제·일괄 조회·변경 판정")
	void inMemoryStore() {
		EgovInMemoryDocumentHashStore store = new EgovInMemoryDocumentHashStore();
		assertTrue(store.isChanged("hwp-a", "h1"), "최초 문서는 변경");
		store.save("hwp-a", "h1");
		store.save("hwp-b", "h2");
		assertEquals(Optional.of("h1"), store.find("hwp-a"));
		assertFalse(store.isChanged("hwp-a", "h1"));
		assertTrue(store.isChanged("hwp-a", "h1-modified"));
		assertEquals(Map.of("hwp-a", "h1", "hwp-b", "h2"), store.findAll(List.of("hwp-a", "hwp-b", "hwp-none")));
		store.remove("hwp-a");
		assertTrue(store.find("hwp-a").isEmpty());
		assertEquals(1, store.size());
	}

	@Test
	@DisplayName("JDBC 저장소 — 동봉 스키마 스크립트 적용 뒤 왕복하고 갱신·삭제·일괄 조회가 동작한다")
	void jdbcStoreRoundTrip() throws Exception {
		EgovJdbcDocumentHashStore store = new EgovJdbcDocumentHashStore(hsqldb("aihash_ok", true));
		String h1 = EgovContentHash.sha256Hex("v1");
		String h2 = EgovContentHash.sha256Hex("v2");
		assertTrue(store.find("pdf-x_1").isEmpty());
		store.save("pdf-x_1", h1);
		assertEquals(Optional.of(h1), store.find("pdf-x_1"));
		store.save("pdf-x_1", h2);
		assertEquals(Optional.of(h2), store.find("pdf-x_1"), "UPDATE 경로");
		store.save("pdf-x_2", h1);
		assertEquals(Map.of("pdf-x_1", h2, "pdf-x_2", h1), store.findAll(List.of("pdf-x_1", "pdf-x_2", "pdf-x_9")));
		assertFalse(store.isChanged("pdf-x_1", h2));
		assertTrue(store.isChanged("pdf-x_1", h1));
		store.remove("pdf-x_1");
		assertTrue(store.find("pdf-x_1").isEmpty());
		assertEquals(EgovJdbcDocumentHashStore.DEFAULT_TABLE, store.table());
	}

	@Test
	@DisplayName("테이블이 없으면 원인을 담은 예외가 전파된다(변경 없음/있음으로 위장하지 않는다)")
	void missingTablePropagates() throws Exception {
		EgovJdbcDocumentHashStore store = new EgovJdbcDocumentHashStore(hsqldb("aihash_missing", false));
		DataAccessException ex = assertThrows(DataAccessException.class, () -> store.find("doc"));
		assertTrue(ex.getCause() != null);
		assertThrows(IllegalArgumentException.class, () -> new EgovJdbcDocumentHashStore(hsqldb("aihash_id", false), "T; DROP"));
	}
}
