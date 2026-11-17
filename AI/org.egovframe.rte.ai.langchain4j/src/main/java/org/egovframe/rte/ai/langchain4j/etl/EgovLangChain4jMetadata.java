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
package org.egovframe.rte.ai.langchain4j.etl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import dev.langchain4j.data.document.Metadata;

/**
 * 어댑터가 세그먼트·문서에 덧붙이는 메타데이터 키 상수와 기반 모듈 메타데이터 → LangChain4j {@link Metadata} 변환 규칙.
 *
 * <p>기반 모듈 키({@code original_id}·{@code file_name} 등)는 그대로 쓰고, 여기에는 파이프라인 단계가 만드는 키만 둔다. LangChain4j
 * {@link Metadata} 는 String·UUID·int·long·float·double 만 받으므로 Boolean 은 "true"/"false" 문자열로, 그 밖의 타입은 {@code String.valueOf}
 * 로 바꿔 넣는다(키를 버리지 않는다). 세그먼트 식별자 키 {@link #SEGMENT_ID}("id")는 기반 모듈 pg_trgm 어휘 검색이 {@code metadata->>'id'}
 * 를 식별자로 돌려주는 계약이다 — 라이터가 저장소 id 와 같은 값을 넣는다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovLangChain4jMetadata {

	/** 세그먼트 순번(0부터, int) — 분할기가 붙인다. */
	public static final String CHUNK_INDEX = "chunk_index";

	/** 같은 원본에서 나온 세그먼트 수(int) — 분할기가 붙인다. */
	public static final String CHUNK_COUNT = "chunk_count";

	/** 청크 수 한도에 걸려 본문 일부가 분할되지 않은 문서의 세그먼트 표식(MARK 정책) — 값은 {@link #TRUE}. */
	public static final String TRUNCATED = "truncated";

	/** 색인용 PII 마스킹이 실제로 치환을 일으킨 문서의 표식 — 값은 {@link #TRUE}. */
	public static final String PII_MASKED = "pii_masked";

	/** 세그먼트 식별자(= 저장소 id) — 라이터가 붙인다. 기반 모듈 pg_trgm 어휘 검색의 식별자 계약. */
	public static final String SEGMENT_ID = "id";

	/** Boolean 표식의 문자열 값. */
	public static final String TRUE = "true";

	private EgovLangChain4jMetadata() {
	}

	/**
	 * 기반 모듈 메타데이터를 LangChain4j {@link Metadata} 로 바꾼다.
	 *
	 * @param source 키 → 값(null 값은 건너뛴다)
	 * @return 새 {@link Metadata}
	 */
	public static Metadata toMetadata(Map<String, ?> source) {
		Metadata metadata = new Metadata();
		if (source == null) {
			return metadata;
		}
		for (Map.Entry<String, ?> entry : source.entrySet()) {
			put(metadata, entry.getKey(), entry.getValue());
		}
		return metadata;
	}

	/**
	 * 값 하나를 타입 규칙대로 넣는다(null 값은 무시).
	 *
	 * @param metadata 대상
	 * @param key 키
	 * @param value 값
	 * @return 대상(체이닝)
	 */
	public static Metadata put(Metadata metadata, String key, Object value) {
		if (key == null || value == null) {
			return metadata;
		}
		if (value instanceof String s) {
			metadata.put(key, s);
		} else if (value instanceof UUID u) {
			metadata.put(key, u);
		} else if (value instanceof Integer i) {
			metadata.put(key, i.intValue());
		} else if (value instanceof Long l) {
			metadata.put(key, l.longValue());
		} else if (value instanceof Float f) {
			metadata.put(key, f.floatValue());
		} else if (value instanceof Double d) {
			metadata.put(key, d.doubleValue());
		} else if (value instanceof Boolean b) {
			metadata.put(key, b ? TRUE : "false");
		} else {
			metadata.put(key, String.valueOf(value));
		}
		return metadata;
	}

	/**
	 * LangChain4j {@link Metadata} 를 맵으로(LangChain4j 제공 그대로, 순서 보존 복사).
	 *
	 * @param metadata 원본(null 이면 빈 맵)
	 * @return 키 → 값
	 */
	public static Map<String, Object> toMap(Metadata metadata) {
		return metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata.toMap());
	}
}
