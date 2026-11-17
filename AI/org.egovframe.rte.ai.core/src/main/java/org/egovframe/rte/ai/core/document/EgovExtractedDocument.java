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
package org.egovframe.rte.ai.core.document;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 프레임워크 중립 문서 값 — 식별자·본문·메타데이터(불변).
 *
 * <p>어댑터가 Spring AI <code>Document</code>·LangChain4j <code>Document</code> 로 1:1 변환한다. 본문은 비어 있을 수 없다
 * (빈 문서는 추출 단계에서 제외되고 그 사실은 결과에 남는다).</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovExtractedDocument {

	private final String id;
	private final String text;
	private final Map<String, Object> metadata;

	/**
	 * @param id 안정 식별자({@link EgovDocumentIds})
	 * @param text 본문(비어 있을 수 없음)
	 * @param metadata 메타데이터(복사됨, null 이면 빈 맵)
	 */
	public EgovExtractedDocument(String id, String text, Map<String, Object> metadata) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("id 는 비어 있을 수 없습니다");
		}
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("본문이 비어 있는 문서는 만들 수 없습니다: " + id);
		}
		this.id = id;
		this.text = text;
		this.metadata = metadata == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
	}

	/** 안정 식별자. */
	public String id() {
		return id;
	}

	/** 본문. */
	public String text() {
		return text;
	}

	/** 메타데이터(불변). */
	public Map<String, Object> metadata() {
		return metadata;
	}

	/** 본문을 바꾼 새 문서(식별자·메타데이터 유지). */
	public EgovExtractedDocument withText(String newText) {
		return new EgovExtractedDocument(id, newText, metadata);
	}

	/** 메타데이터 항목을 더한 새 문서. */
	public EgovExtractedDocument withMetadata(String key, Object value) {
		Map<String, Object> copy = new LinkedHashMap<>(metadata);
		copy.put(Objects.requireNonNull(key, "key must not be null"), value);
		return new EgovExtractedDocument(id, text, copy);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof EgovExtractedDocument)) {
			return false;
		}
		EgovExtractedDocument that = (EgovExtractedDocument) o;
		return id.equals(that.id) && text.equals(that.text) && metadata.equals(that.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, text, metadata);
	}

	@Override
	public String toString() {
		return "EgovExtractedDocument[id=" + id + ", length=" + text.length() + ", metadata=" + metadata.keySet() + "]";
	}
}
