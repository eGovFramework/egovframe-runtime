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
package org.egovframe.rte.ai.springai.etl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 재색인 라이터 — 같은 원본({@code original_id})의 기존 청크를 지운 뒤 새 청크를 저장한다.
 *
 * <p>삭제는 {@link VectorStore#delete(org.springframework.ai.vectorstore.filter.Filter.Expression)} 의 IN 조건 한 가지 경로다
 * — 저장소 종류(Redis·pgvector·…)별 코드가 없고, RediSearch TAG 이스케이프 같은 저장소 사정은 Spring AI 저장소 구현이 담당한다
 * (id 는 기반 모듈 {@code EgovDocumentIds} 가 정리한 값). {@code original_id} 가 없는 문서는 기본적으로 예외로 거부하고, 허용
 * 옵션을 켠 경우에만 stale 정리 없이 저장하며 경고를 남긴다. 삭제 IN 목록과 추가 목록은 배치 크기로 나눈다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 샘플 라이터의 저장소별 분기·침묵 경고를 일반 경로·예외로)
 *  2026.09.03  실행환경팀     필터 삭제 미지원 저장소(SimpleVectorStore)의 UnsupportedOperationException 을 안내 예외로 감쌈(4/4 예제 실측)
 * 2026.09.08	실행환경 개발팀		보안 재점검(6차): original_id 덮어쓰기(CWE-639) 경고 javadoc
 * </pre>
 *
 * <p><b>보안 주의(2026-09-08 보안 재점검, CWE-639)</b>: 재색인은 {@code original_id} 가 같은 세그먼트를 <b>먼저 지우고</b>
 * 새로 넣는다. 이 식별자가 업로드 파일명에서 그대로 파생되면(예: {@code EgovDocumentIds}) 다른 사용자가 같은 파일명으로
 * 올리는 것만으로 남의 색인을 덮어쓸 수 있다 — 소유자·테넌트 접두를 붙인 식별자만 넘기거나 저장소를 분리한다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovVectorStoreWriter implements DocumentWriter {

	private static final Logger LOG = LoggerFactory.getLogger(EgovVectorStoreWriter.class);

	/** 삭제 IN 목록 기본 배치 크기(RediSearch 질의 길이 한도 여유). */
	public static final int DEFAULT_DELETE_BATCH_SIZE = 200;

	/** 추가 기본 배치 크기(임베딩 호출 단위). */
	public static final int DEFAULT_ADD_BATCH_SIZE = 100;

	private final VectorStore vectorStore;
	private final int deleteBatchSize;
	private final int addBatchSize;
	private final boolean allowMissingOriginalId;

	private EgovVectorStoreWriter(Builder builder) {
		this.vectorStore = builder.vectorStore;
		this.deleteBatchSize = builder.deleteBatchSize;
		this.addBatchSize = builder.addBatchSize;
		this.allowMissingOriginalId = builder.allowMissingOriginalId;
	}

	/**
	 * 빌더.
	 *
	 * @param vectorStore 대상 저장소
	 * @return 빌더
	 */
	public static Builder builder(VectorStore vectorStore) {
		return new Builder(vectorStore);
	}

	/**
	 * 기본 옵션의 라이터.
	 *
	 * @param vectorStore 대상 저장소
	 * @return 라이터
	 */
	public static EgovVectorStoreWriter of(VectorStore vectorStore) {
		return builder(vectorStore).build();
	}

	/**
	 * stale 청크를 지운 뒤 저장한다.
	 *
	 * @param documents 청크 문서(빈 목록은 저장소를 호출하지 않는다)
	 * @throws IllegalArgumentException {@code original_id} 가 없는 문서가 있고 허용 옵션이 꺼져 있을 때(저장소 호출 전에 검사)
	 * @throws IllegalStateException 저장소가 필터 삭제를 지원하지 않을 때({@code SimpleVectorStore} 등 — 원인 예외 포함)
	 */
	@Override
	public void accept(List<Document> documents) {
		Objects.requireNonNull(documents, "documents must not be null");
		if (documents.isEmpty()) {
			return;
		}
		Set<String> originalIds = new LinkedHashSet<>();
		int missing = 0;
		for (Document document : documents) {
			Object originalId = document.getMetadata().get(EgovDocumentMetadata.ORIGINAL_ID);
			if (originalId == null || originalId.toString().isBlank()) {
				if (!allowMissingOriginalId) {
					throw new IllegalArgumentException("문서 '" + document.getId() + "' 에 " + EgovDocumentMetadata.ORIGINAL_ID
							+ " 메타데이터가 없어 stale 청크를 정리할 수 없습니다. 리더·분할기가 original_id 를 넣게 하거나, 정리 없이 저장하려면 "
							+ "allowMissingOriginalId(true) 를 명시하세요");
				}
				missing++;
			} else {
				originalIds.add(originalId.toString());
			}
		}
		if (missing > 0) {
			LOG.warn("{} 없는 문서 {}건 — stale 청크 정리 없이 저장합니다", EgovDocumentMetadata.ORIGINAL_ID, missing);
		}
		List<String> ids = new ArrayList<>(originalIds);
		for (int from = 0; from < ids.size(); from += deleteBatchSize) {
			List<Object> batch = new ArrayList<>(ids.subList(from, Math.min(from + deleteBatchSize, ids.size())));
			try {
				vectorStore.delete(new FilterExpressionBuilder().in(EgovDocumentMetadata.ORIGINAL_ID, batch).build());
			} catch (UnsupportedOperationException e) {
				throw new IllegalStateException("벡터 저장소 " + vectorStore.getClass().getSimpleName() + " 이(가) 필터 삭제(delete(Filter.Expression))를 "
						+ "지원하지 않아 stale 청크를 정리할 수 없습니다 — Spring AI SimpleVectorStore 가 대표적입니다. 필터 삭제를 지원하는 저장소"
						+ "(pgvector·Redis 등)를 쓰거나 doDelete(Filter.Expression) 을 구현한 저장소를 넘기세요", e);
			}
		}
		for (int from = 0; from < documents.size(); from += addBatchSize) {
			vectorStore.add(new ArrayList<>(documents.subList(from, Math.min(from + addBatchSize, documents.size()))));
		}
	}

	/** {@link EgovVectorStoreWriter} 빌더. */
	public static final class Builder {
		private final VectorStore vectorStore;
		private int deleteBatchSize = DEFAULT_DELETE_BATCH_SIZE;
		private int addBatchSize = DEFAULT_ADD_BATCH_SIZE;
		private boolean allowMissingOriginalId = false;

		private Builder(VectorStore vectorStore) {
			this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore must not be null");
		}

		/** 삭제 IN 목록 배치 크기(1 이상, 기본 {@value #DEFAULT_DELETE_BATCH_SIZE}). */
		public Builder deleteBatchSize(int deleteBatchSize) {
			this.deleteBatchSize = deleteBatchSize;
			return this;
		}

		/** 추가 배치 크기(1 이상, 기본 {@value #DEFAULT_ADD_BATCH_SIZE}). */
		public Builder addBatchSize(int addBatchSize) {
			this.addBatchSize = addBatchSize;
			return this;
		}

		/** {@code original_id} 없는 문서를 stale 정리 없이 저장할지(기본 false — 예외). */
		public Builder allowMissingOriginalId(boolean allow) {
			this.allowMissingOriginalId = allow;
			return this;
		}

		public EgovVectorStoreWriter build() {
			if (deleteBatchSize < 1) {
				throw new IllegalArgumentException("deleteBatchSize 는 1 이상이어야 합니다: " + deleteBatchSize);
			}
			if (addBatchSize < 1) {
				throw new IllegalArgumentException("addBatchSize 는 1 이상이어야 합니다: " + addBatchSize);
			}
			return new EgovVectorStoreWriter(this);
		}
	}
}
