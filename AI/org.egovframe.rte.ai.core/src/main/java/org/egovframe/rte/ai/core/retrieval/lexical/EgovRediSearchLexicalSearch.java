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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.egovframe.rte.ai.core.retrieval.EgovLexicalSearch;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

/**
 * RediSearch(<code>FT.SEARCH</code>) 어휘 검색 백엔드 — jedis(optional) 기반.
 *
 * <p>{@link EgovRediSearchQueries} 의 3단 질의를 순서대로 실행해 회수가 생긴 첫 단계의 식별자 순위를 돌려준다. 벡터 저장소가
 * 색인한 문서 키(<code>embedding:&lt;id&gt;</code>)에서 접두어를 벗겨 밀집 채널의 식별자와 같은 값이 되게 한다. 실행부는
 * {@link Executor} 로 분리되어 Redis 없이 단계 폴백을 단위 검증할 수 있다. Redis 오류는 jedis 예외 그대로 전파된다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovRediSearchLexicalSearch implements EgovLexicalSearch {

	/** RedisVectorStore 기본 문서 키 접두어. */
	public static final String DEFAULT_KEY_PREFIX = "embedding:";

	/** 질의 실행부 — 질의 문자열과 상한으로 문서 키 목록을 돌려준다(회수 0 이면 빈 목록). */
	@FunctionalInterface
	public interface Executor {
		List<String> execute(String query, int topK);
	}

	private final Executor executor;
	private final String keyPrefix;
	private final EgovRediSearchQueries queries;

	/**
	 * 운영용 — jedis 로 인덱스를 검색한다.
	 *
	 * @param jedis 풀링 클라이언트
	 * @param indexName RediSearch 인덱스명(밀집 채널과 같은 인덱스)
	 * @param keyPrefix 문서 키 접두어(보통 {@link #DEFAULT_KEY_PREFIX})
	 * @param queries 질의 빌더(본문 필드명)
	 */
	public EgovRediSearchLexicalSearch(JedisPooled jedis, String indexName, String keyPrefix,
			EgovRediSearchQueries queries) {
		this(new JedisExecutor(jedis, indexName), keyPrefix, queries);
	}

	/**
	 * 실행부 주입 — 테스트·대체 클라이언트용.
	 */
	public EgovRediSearchLexicalSearch(Executor executor, String keyPrefix, EgovRediSearchQueries queries) {
		this.executor = Objects.requireNonNull(executor, "executor must not be null");
		this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
		this.queries = Objects.requireNonNull(queries, "queries must not be null");
	}

	@Override
	public List<String> search(String query, int topK) {
		int limit = Math.max(topK, 0);
		if (limit == 0) {
			return List.of();
		}
		for (String staged : queries.stagedQueries(query)) {
			List<String> keys = executor.execute(staged, limit);
			if (keys != null && !keys.isEmpty()) {
				List<String> ids = new ArrayList<>(keys.size());
				for (String key : keys) {
					ids.add(EgovRediSearchQueries.stripPrefix(key, keyPrefix));
				}
				return List.copyOf(ids);
			}
		}
		return List.of();
	}

	/** jedis 실행부 — jedis 타입 참조를 이 안에 가둔다. */
	private static final class JedisExecutor implements Executor {
		private final JedisPooled jedis;
		private final String indexName;

		JedisExecutor(JedisPooled jedis, String indexName) {
			this.jedis = Objects.requireNonNull(jedis, "jedis must not be null");
			if (indexName == null || indexName.isBlank()) {
				throw new IllegalArgumentException("indexName 은 비어 있을 수 없습니다");
			}
			this.indexName = indexName;
		}

		@Override
		public List<String> execute(String query, int topK) {
			Query searchQuery = new Query(query).limit(0, topK).setNoContent();
			SearchResult result = jedis.ftSearch(indexName, searchQuery);
			if (result.getTotalResults() <= 0) {
				return List.of();
			}
			List<String> keys = new ArrayList<>();
			for (redis.clients.jedis.search.Document document : result.getDocuments()) {
				keys.add(document.getId());
			}
			return keys;
		}
	}
}
