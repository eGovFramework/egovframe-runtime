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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 메모리 문서 해시 저장소 — 단일 인스턴스·개발·테스트용(재기동 시 소실).
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovInMemoryDocumentHashStore implements EgovDocumentHashStore {

	private final Map<String, String> hashes = new ConcurrentHashMap<>();

	@Override
	public Optional<String> find(String documentId) {
		return Optional.ofNullable(hashes.get(Objects.requireNonNull(documentId, "documentId must not be null")));
	}

	@Override
	public void save(String documentId, String contentHash) {
		hashes.put(Objects.requireNonNull(documentId, "documentId must not be null"),
				Objects.requireNonNull(contentHash, "contentHash must not be null"));
	}

	@Override
	public void remove(String documentId) {
		hashes.remove(Objects.requireNonNull(documentId, "documentId must not be null"));
	}

	@Override
	public Map<String, String> findAll(Collection<String> documentIds) {
		Map<String, String> found = new LinkedHashMap<>();
		if (documentIds != null) {
			for (String id : documentIds) {
				String hash = hashes.get(id);
				if (hash != null) {
					found.put(id, hash);
				}
			}
		}
		return found;
	}

	/** 저장된 항목 수. */
	public int size() {
		return hashes.size();
	}
}
