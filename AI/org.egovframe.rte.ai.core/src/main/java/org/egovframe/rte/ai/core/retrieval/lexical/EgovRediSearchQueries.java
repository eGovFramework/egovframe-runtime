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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * RediSearch 어휘 질의 빌더 — 순수 함수(Redis 비의존, 단위 테스트 가능).
 *
 * <p>RediSearch 기본 토크나이저는 한글을 형태소가 아닌 공백·문장부호 단위 어절로 색인하므로, 회수율을 위해 질의를
 * 3단으로 만든다: 정확(어절 전체 AND) → 접두(<code>어절*</code> OR) → 중위(<code>*어절*</code> OR). 호출 측은 회수가 생긴 첫
 * 단계를 채택한다. 질의 구문 특수문자는 백슬래시로 이스케이프한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플 EgovLexicalQueryBuilder 설계 차용)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovRediSearchQueries {

	/** 기본 본문(TEXT) 필드명 — Spring AI RedisVectorStore 기본값. */
	public static final String DEFAULT_CONTENT_FIELD = "content";

	/** 기본 필드명을 쓰는 빌더. */
	public static final EgovRediSearchQueries DEFAULT = new EgovRediSearchQueries(DEFAULT_CONTENT_FIELD);

	/** RediSearch 질의 구문에서 의미를 갖는 특수문자(이스케이프 대상). */
	private static final String SPECIAL_CHARS = ",.<>{}[]\"':;!@#$%^&*()-+=~|/\\ ";

	private final String contentField;

	/**
	 * @param contentField 본문 TEXT 필드명
	 */
	public EgovRediSearchQueries(String contentField) {
		if (contentField == null || contentField.isBlank()) {
			throw new IllegalArgumentException("contentField 는 비어 있을 수 없습니다");
		}
		this.contentField = contentField;
	}

	/** 본문 필드명. */
	public String contentField() {
		return contentField;
	}

	/**
	 * 질의 어절 수 상한 — 수만 개 어절이 중위(*t*) OR 질의 하나로 조립돼 RediSearch 를 잠그지 못하게 한다
	 * (CWE-400, 2026-09-07 보안 재점검). 상한을 넘는 뒤쪽 어절은 버린다.
	 */
	public static final int MAX_TERMS = 32;

	/**
	 * 질의를 공백 어절로 분해한다(등장 순서 보존·중복 제거·빈 토큰 제거, 최대 {@value #MAX_TERMS}개).
	 *
	 * @param query 사용자 질의(null·공백이면 빈 목록)
	 */
	public static List<String> tokenize(String query) {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		Set<String> terms = new LinkedHashSet<>();
		for (String raw : query.trim().split("\\s+")) {
			String term = raw.trim();
			if (!term.isEmpty()) {
				terms.add(term);
				if (terms.size() >= MAX_TERMS) {
					break;
				}
			}
		}
		return List.copyOf(terms);
	}

	/**
	 * [정확, 접두, 중위] 순의 질의 문자열(어절이 없으면 빈 목록).
	 */
	public List<String> stagedQueries(String query) {
		List<String> terms = tokenize(query);
		if (terms.isEmpty()) {
			return List.of();
		}
		return List.of(exactQuery(terms), prefixQuery(terms), infixQuery(terms));
	}

	/** 정확 일치 질의: <code>@content:(t1 t2 …)</code> — 어절 전부 포함(AND). */
	public String exactQuery(List<String> terms) {
		Objects.requireNonNull(terms, "terms must not be null");
		StringBuilder sb = new StringBuilder("@").append(contentField).append(":(");
		for (int i = 0; i < terms.size(); i++) {
			if (i > 0) {
				sb.append(' ');
			}
			sb.append(escape(terms.get(i)));
		}
		return sb.append(')').toString();
	}

	/** 접두 일치 질의: <code>@content:(t1* | t2* | …)</code>. */
	public String prefixQuery(List<String> terms) {
		return wildcardQuery(terms, "", "*");
	}

	/** 중위 일치 질의: <code>@content:(*t1* | *t2* | …)</code>. */
	public String infixQuery(List<String> terms) {
		return wildcardQuery(terms, "*", "*");
	}

	private String wildcardQuery(List<String> terms, String pre, String post) {
		Objects.requireNonNull(terms, "terms must not be null");
		StringBuilder sb = new StringBuilder("@").append(contentField).append(":(");
		for (int i = 0; i < terms.size(); i++) {
			if (i > 0) {
				sb.append(" | ");
			}
			sb.append(pre).append(escape(terms.get(i))).append(post);
		}
		return sb.append(')').toString();
	}

	/**
	 * RediSearch 질의 특수문자를 백슬래시로 이스케이프한다(와일드카드는 호출 측이 붙인다).
	 */
	public static String escape(String term) {
		Objects.requireNonNull(term, "term must not be null");
		StringBuilder sb = new StringBuilder(term.length() + 4);
		for (int i = 0; i < term.length(); i++) {
			char c = term.charAt(i);
			if (SPECIAL_CHARS.indexOf(c) >= 0) {
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * 저장소 키 접두어(예: <code>embedding:</code>)를 벗겨 순수 식별자로 만든다.
	 */
	public static String stripPrefix(String key, String prefix) {
		if (key != null && prefix != null && !prefix.isEmpty() && key.startsWith(prefix)) {
			return key.substring(prefix.length());
		}
		return key;
	}
}
