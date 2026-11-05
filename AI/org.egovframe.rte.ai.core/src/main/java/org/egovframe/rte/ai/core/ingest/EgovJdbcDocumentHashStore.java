/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.ai.core.ingest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JDBC 문서 해시 저장소 — spring-jdbc(optional) 기반.
 *
 * <p>테이블은 <code>egov/ai/ingest/schema-hsqldb.sql</code>(HSQLDB)·<code>schema-ansi.sql</code>(일반) 스크립트로 운영자가
 * 만든다 — <b>기동 시 DDL 을 자동 실행하지 않는다.</b> 갱신은 UPDATE 후 0건이면 INSERT(방언 비종속). 저장소 오류는
 * {@code DataAccessException}(unchecked, 원인 포함) 그대로 전파된다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 저장소 SPI 관례의 Jdbc 구현)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovJdbcDocumentHashStore implements EgovDocumentHashStore {

	/** 기본 테이블명(스키마 스크립트와 동일). */
	public static final String DEFAULT_TABLE = "EGOV_AI_DOC_HASH";

	private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_.]*");
	private static final int IN_BATCH = 200;

	private final JdbcTemplate jdbc;
	private final String selectSql;
	private final String updateSql;
	private final String insertSql;
	private final String deleteSql;
	private final String table;

	/** 기본 테이블명으로 만든다. */
	public EgovJdbcDocumentHashStore(JdbcTemplate jdbc) {
		this(jdbc, DEFAULT_TABLE);
	}

	/**
	 * @param jdbc JDBC 템플릿
	 * @param table 테이블명(식별자 문자만)
	 */
	public EgovJdbcDocumentHashStore(JdbcTemplate jdbc, String table) {
		this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
		if (table == null || !IDENTIFIER.matcher(table).matches()) {
			throw new IllegalArgumentException("table 은 SQL 식별자 문자만 허용합니다: " + table);
		}
		this.table = table;
		this.selectSql = "SELECT CONTENT_HASH FROM " + table + " WHERE DOC_ID = ?";
		this.updateSql = "UPDATE " + table + " SET CONTENT_HASH = ?, UPDATED_AT = ? WHERE DOC_ID = ?";
		this.insertSql = "INSERT INTO " + table + " (DOC_ID, CONTENT_HASH, UPDATED_AT) VALUES (?, ?, ?)";
		this.deleteSql = "DELETE FROM " + table + " WHERE DOC_ID = ?";
	}

	/** 테이블명. */
	public String table() {
		return table;
	}

	@Override
	public Optional<String> find(String documentId) {
		Objects.requireNonNull(documentId, "documentId must not be null");
		List<String> rows = jdbc.query(selectSql, (rs, rowNum) -> rs.getString(1), documentId);
		return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.get(0));
	}

	@Override
	public void save(String documentId, String contentHash) {
		Objects.requireNonNull(documentId, "documentId must not be null");
		Objects.requireNonNull(contentHash, "contentHash must not be null");
		Timestamp now = new Timestamp(System.currentTimeMillis());
		int updated = jdbc.update(updateSql, contentHash, now, documentId);
		if (updated == 0) {
			jdbc.update(insertSql, documentId, contentHash, now);
		}
	}

	@Override
	public void remove(String documentId) {
		Objects.requireNonNull(documentId, "documentId must not be null");
		jdbc.update(deleteSql, documentId);
	}

	@Override
	public Map<String, String> findAll(Collection<String> documentIds) {
		Map<String, String> found = new LinkedHashMap<>();
		if (documentIds == null || documentIds.isEmpty()) {
			return found;
		}
		List<String> ids = new ArrayList<>(documentIds);
		for (int from = 0; from < ids.size(); from += IN_BATCH) {
			List<String> batch = ids.subList(from, Math.min(from + IN_BATCH, ids.size()));
			StringBuilder sql = new StringBuilder("SELECT DOC_ID, CONTENT_HASH FROM ").append(table).append(" WHERE DOC_ID IN (");
			for (int i = 0; i < batch.size(); i++) {
				sql.append(i == 0 ? "?" : ", ?");
			}
			sql.append(')');
			jdbc.query(sql.toString(), rs -> {
				found.put(rs.getString(1), rs.getString(2));
			}, batch.toArray());
		}
		// 요청 순서를 보존한 결과
		Map<String, String> ordered = new LinkedHashMap<>();
		for (String id : ids) {
			String hash = found.get(id);
			if (hash != null) {
				ordered.put(id, hash);
			}
		}
		return ordered;
	}
}
