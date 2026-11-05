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
package org.egovframe.rte.ai.core.retrieval.lexical;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.egovframe.rte.ai.core.retrieval.EgovLexicalSearch;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * PostgreSQL <code>pg_trgm</code> 어휘 검색 백엔드 — spring-jdbc(optional) 기반.
 *
 * <p>대칭 유사도(<code>%</code>)는 청크가 길수록 trigram Jaccard 분모가 커져 값이 붕괴한다(4000자 청크에서 정답 유사도
 * 0.006~0.058 실측 — 샘플 주석). 단어 유사도(<code>word_similarity</code>, <code>%&gt;</code>)는 질의를 문서의 "가장 잘 맞는 연속
 * 구간"과 비교하므로 청크 길이에 강건하다. 임계값은 <code>set_config(…, true)</code> 로 <b>트랜잭션 범위에만</b> 적용해 커넥션
 * 풀에 누수되지 않게 한다. 확장·인덱스 DDL 은 실행하지 않는다 — <code>egov/ai/retrieval/schema-postgresql-pg_trgm.sql</code>
 * 을 DBA 가 적용한다.</p>
 *
 * <p>융합 키는 메타데이터 JSON 의 <code>id</code>(<code>metadata::jsonb -&gt;&gt; 'id'</code>) — 밀집 채널의 식별자와 같은 문자열이
 * 되도록 SQL 에서 추출한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플 EgovHybridContentRetriever 의 lexical 부분 설계 차용,
 *                            기동 시 DDL 자동 실행은 제거)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovPgTrgmLexicalSearch implements EgovLexicalSearch {

	/** 임계값 GUC 를 트랜잭션 범위(is_local=true)로 설정하는 SQL. */
	public static final String SET_THRESHOLD_SQL =
			"SELECT set_config('pg_trgm.word_similarity_threshold', ?, true)";

	/** 기본 본문 컬럼(LangChain4j PgVectorEmbeddingStore 기본값). Spring AI PgVectorStore 는 <code>content</code>. */
	public static final String DEFAULT_TEXT_COLUMN = "text";

	/** 기본 메타데이터 컬럼. */
	public static final String DEFAULT_METADATA_COLUMN = "metadata";

	private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_.]*");

	private final JdbcTemplate jdbc;
	private final TransactionTemplate transaction;
	private final String sql;
	private final double threshold;

	/**
	 * @param jdbc JDBC 템플릿
	 * @param transactionManager 임계값을 트랜잭션 범위로 묶을 트랜잭션 매니저
	 * @param table 임베딩 테이블(식별자 문자만 허용 — SQL 에 직접 들어간다)
	 * @param textColumn 본문 컬럼
	 * @param metadataColumn 메타데이터(JSON) 컬럼
	 * @param threshold word_similarity 임계값(0.0~1.0, 한국어 긴 청크는 0.30 부근이 실측 최적)
	 */
	public EgovPgTrgmLexicalSearch(JdbcTemplate jdbc, PlatformTransactionManager transactionManager, String table,
			String textColumn, String metadataColumn, double threshold) {
		this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
		this.transaction = new TransactionTemplate(Objects.requireNonNull(transactionManager, "transactionManager must not be null"));
		if (threshold < 0.0 || threshold > 1.0) {
			throw new IllegalArgumentException("threshold 는 0.0~1.0 이어야 합니다: " + threshold);
		}
		this.threshold = threshold;
		this.sql = selectSql(table, textColumn, metadataColumn);
	}

	/** 기본 컬럼(text·metadata)으로 만든다. */
	public EgovPgTrgmLexicalSearch(JdbcTemplate jdbc, PlatformTransactionManager transactionManager, String table,
			double threshold) {
		this(jdbc, transactionManager, table, DEFAULT_TEXT_COLUMN, DEFAULT_METADATA_COLUMN, threshold);
	}

	/**
	 * 검색 SQL 을 조립한다 — <code>%&gt;</code> 로 후보를 거르고 word_similarity 내림차순, 식별자는 metadata JSON 의 id.
	 *
	 * @throws IllegalArgumentException 테이블·컬럼이 식별자 문자가 아님(SQL 주입 방지)
	 */
	public static String selectSql(String table, String textColumn, String metadataColumn) {
		requireIdentifier(table, "table");
		requireIdentifier(textColumn, "textColumn");
		requireIdentifier(metadataColumn, "metadataColumn");
		return "SELECT " + metadataColumn + "::jsonb ->> 'id' AS doc_id FROM " + table
				+ " WHERE " + textColumn + " %> ? ORDER BY word_similarity(?, " + textColumn + ") DESC LIMIT ?";
	}

	/** 조립된 검색 SQL. */
	public String sql() {
		return sql;
	}

	/** 임계값. */
	public double threshold() {
		return threshold;
	}

	@Override
	public List<String> search(String query, int topK) {
		int limit = Math.max(topK, 0);
		if (limit == 0 || query == null || query.isBlank()) {
			return List.of();
		}
		List<String> ids = transaction.execute(status -> {
			// set_config 는 값을 돌려주는 SELECT 라 queryForObject 로 실행한다(반환값은 쓰지 않는다)
			jdbc.queryForObject(SET_THRESHOLD_SQL, String.class, Double.toString(threshold));
			return jdbc.query(sql, (rs, rowNum) -> rs.getString("doc_id"), query, query, limit);
		});
		return ids == null ? List.of() : List.copyOf(ids);
	}

	private static void requireIdentifier(String value, String name) {
		if (value == null || !IDENTIFIER.matcher(value).matches()) {
			throw new IllegalArgumentException(name + " 은 SQL 식별자 문자([A-Za-z_][A-Za-z0-9_.]*)만 허용합니다: " + value);
		}
	}
}
