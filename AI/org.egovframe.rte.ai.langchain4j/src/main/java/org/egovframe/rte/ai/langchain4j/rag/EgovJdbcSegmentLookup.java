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
package org.egovframe.rte.ai.langchain4j.rag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.egovframe.rte.ai.langchain4j.etl.EgovLangChain4jMetadata;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

/**
 * JDBC 세그먼트 조회 — pgvector 저장소 표 규약(식별자·본문·metadata JSON 컬럼)을 방언 중립 SQL 로 읽는 {@link EgovSegmentLookup} 구현.
 *
 * <p>pgvector 의 식별자 컬럼({@code embedding_id})은 uuid 타입이라 문자열 파라미터 IN 이 실패한다({@code uuid = varchar}). 그래서
 * {@code CAST(식별자 AS VARCHAR(64)) IN (?, …)} 로 읽는다 — PostgreSQL·HSQLDB 모두에서 동작해 테스트와 운영이 같은 SQL 을 쓴다. metadata
 * JSON 은 Jackson 으로 맵을 만들어 {@link EgovLangChain4jMetadata#toMetadata(Map)} 규칙으로 복원한다. 컬럼 기본값은 LangChain4j
 * {@code PgVectorEmbeddingStore}(표 {@code document_embeddings}, {@code embedding_id}·{@code text}·{@code metadata}). optional 의존
 * {@code spring-jdbc} 가 필요하다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 샘플의 pg_trgm SQL 본문 조회를 SPI 구현으로 분리)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovJdbcSegmentLookup implements EgovSegmentLookup {

	/** LangChain4j PgVectorEmbeddingStore 기본 표. */
	public static final String DEFAULT_TABLE = "document_embeddings";

	/** 기본 식별자 컬럼. */
	public static final String DEFAULT_ID_COLUMN = "embedding_id";

	/** 기본 본문 컬럼. */
	public static final String DEFAULT_TEXT_COLUMN = "text";

	/** 기본 metadata JSON 컬럼. */
	public static final String DEFAULT_METADATA_COLUMN = "metadata";

	private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_.]*");
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final JdbcTemplate jdbc;
	private final String table;
	private final String idColumn;
	private final String textColumn;
	private final String metadataColumn;
	private final ObjectMapper objectMapper;

	private EgovJdbcSegmentLookup(Builder builder) {
		this.jdbc = builder.jdbc;
		this.table = identifier(builder.table, "table");
		this.idColumn = identifier(builder.idColumn, "idColumn");
		this.textColumn = identifier(builder.textColumn, "textColumn");
		this.metadataColumn = identifier(builder.metadataColumn, "metadataColumn");
		this.objectMapper = builder.objectMapper == null ? new ObjectMapper() : builder.objectMapper;
	}

	/**
	 * 빌더.
	 *
	 * @param jdbc JdbcTemplate
	 * @return 빌더
	 */
	public static Builder builder(JdbcTemplate jdbc) {
		return new Builder(jdbc);
	}

	/**
	 * 기본 표·컬럼의 조회.
	 *
	 * @param jdbc JdbcTemplate
	 * @return 조회
	 */
	public static EgovJdbcSegmentLookup of(JdbcTemplate jdbc) {
		return builder(jdbc).build();
	}

	/**
	 * 조회 SQL(식별자 CAST 후 IN).
	 *
	 * @param count IN 항목 수(1 이상)
	 * @return SQL
	 */
	public String selectSql(int count) {
		StringBuilder sql = new StringBuilder("SELECT ").append(idColumn).append(", ").append(textColumn).append(", ")
				.append(metadataColumn).append(" FROM ").append(table).append(" WHERE CAST(").append(idColumn)
				.append(" AS VARCHAR(64)) IN (");
		for (int i = 0; i < count; i++) {
			sql.append(i == 0 ? "?" : ", ?");
		}
		return sql.append(')').toString();
	}

	@Override
	public Map<String, TextSegment> findByIds(List<String> ids) {
		Objects.requireNonNull(ids, "ids must not be null");
		Map<String, TextSegment> found = new LinkedHashMap<>();
		if (ids.isEmpty()) {
			return found;
		}
		jdbc.query(selectSql(ids.size()), rs -> {
			String id = rs.getString(1);
			String text = rs.getString(2);
			String json = rs.getString(3);
			found.put(id, new TextSegment(text == null ? "" : text, toMetadata(json)));
		}, ids.toArray());
		return found;
	}

	private Metadata toMetadata(String json) {
		if (json == null || json.isBlank()) {
			return new Metadata();
		}
		try {
			return EgovLangChain4jMetadata.toMetadata(objectMapper.readValue(json, MAP_TYPE));
		} catch (java.io.IOException e) {
			throw new IllegalStateException("세그먼트 metadata JSON 을 읽지 못했습니다: " + e.getMessage(), e);
		}
	}

	private static String identifier(String value, String name) {
		if (value == null || !IDENTIFIER.matcher(value).matches()) {
			throw new IllegalArgumentException(name + " 은 SQL 식별자여야 합니다: " + value);
		}
		return value;
	}

	/** {@link EgovJdbcSegmentLookup} 빌더. */
	public static final class Builder {
		private final JdbcTemplate jdbc;
		private String table = DEFAULT_TABLE;
		private String idColumn = DEFAULT_ID_COLUMN;
		private String textColumn = DEFAULT_TEXT_COLUMN;
		private String metadataColumn = DEFAULT_METADATA_COLUMN;
		private ObjectMapper objectMapper;

		private Builder(JdbcTemplate jdbc) {
			this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
		}

		/** 표 이름(기본 {@value #DEFAULT_TABLE}). */
		public Builder table(String table) {
			this.table = table;
			return this;
		}

		/** 식별자 컬럼(기본 {@value #DEFAULT_ID_COLUMN}). */
		public Builder idColumn(String idColumn) {
			this.idColumn = idColumn;
			return this;
		}

		/** 본문 컬럼(기본 {@value #DEFAULT_TEXT_COLUMN}). */
		public Builder textColumn(String textColumn) {
			this.textColumn = textColumn;
			return this;
		}

		/** metadata JSON 컬럼(기본 {@value #DEFAULT_METADATA_COLUMN}). */
		public Builder metadataColumn(String metadataColumn) {
			this.metadataColumn = metadataColumn;
			return this;
		}

		/** metadata JSON 파서(기본 새 {@link ObjectMapper}). */
		public Builder objectMapper(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
			return this;
		}

		public EgovJdbcSegmentLookup build() {
			return new EgovJdbcSegmentLookup(this);
		}
	}
}
