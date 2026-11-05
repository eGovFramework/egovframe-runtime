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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 문서 해시 저장소 SPI — 문서 식별자별 본문 해시를 저장해 재색인 시 변경 문서만 처리하게 한다.
 *
 * <p>구현: {@link EgovInMemoryDocumentHashStore}(단일 인스턴스·테스트) · {@link EgovJdbcDocumentHashStore}(JDBC). 저장소 오류는
 * 예외로 전파한다 — "변경 없음"이나 "변경 있음"으로 위장하면 색인 누락·전량 재색인이 조용히 일어난다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public interface EgovDocumentHashStore {

	/** 저장된 해시(없으면 empty). */
	Optional<String> find(String documentId);

	/** 해시를 저장(갱신)한다. */
	void save(String documentId, String contentHash);

	/** 해시를 지운다(없어도 예외 아님). */
	void remove(String documentId);

	/** 여러 식별자의 해시를 한 번에 조회한다(없는 식별자는 결과에 없음). */
	Map<String, String> findAll(Collection<String> documentIds);

	/**
	 * 변경 판정 — 저장된 해시가 없거나 현재 해시와 다르면 변경.
	 */
	default boolean isChanged(String documentId, String currentHash) {
		Optional<String> stored = find(documentId);
		return stored.isEmpty() || !stored.get().equals(currentHash);
	}
}
