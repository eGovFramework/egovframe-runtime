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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;

/**
 * 재색인 라이터 — 같은 원본({@code original_id})의 기존 세그먼트를 지운 뒤 세그먼트 id 를 정해 임베딩·저장한다.
 *
 * <ul>
 *   <li>삭제는 {@link EmbeddingStore#removeAll(dev.langchain4j.store.embedding.filter.Filter)} 의 IN 조건 한 경로(pgvector·InMemory 구현).</li>
 *   <li>세그먼트 id 는 라이터가 UUID 로 <b>사전 생성</b>해 메타데이터 {@code id}({@link EgovLangChain4jMetadata#SEGMENT_ID})에 넣고 같은 id 로
 *       {@code addAll(ids, embeddings, segments)} 한다 — 기반 모듈 pg_trgm 어휘 검색이 {@code metadata->>'id'} 를 식별자로 돌려주므로
 *       저장소 id 와 메타데이터 id 가 같아야 두 채널의 식별자가 맞는다.</li>
 *   <li>임베딩은 {@link EmbeddingModel#embedAll} 배치, 삭제 IN 목록도 배치. {@code original_id} 없는 세그먼트는 기본 예외(저장소·모델 호출 전 검사),
 *       허용 옵션을 켠 경우만 정리 없이 저장하고 경고를 남긴다.</li>
 * </ul>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 샘플 라이터의 metadataKey("id") 규약을 original_id/id 로 정리)
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
public final class EgovEmbeddingStoreWriter {

	private static final Logger LOG = LoggerFactory.getLogger(EgovEmbeddingStoreWriter.class);

	/** 삭제 IN 목록 기본 배치 크기. */
	public static final int DEFAULT_DELETE_BATCH_SIZE = 200;

	/** 임베딩·추가 기본 배치 크기(모델 서버 요청 크기 한도 여유). */
	public static final int DEFAULT_EMBED_BATCH_SIZE = 50;

	private final EmbeddingStore<TextSegment> embeddingStore;
	private final EmbeddingModel embeddingModel;
	private final int deleteBatchSize;
	private final int embedBatchSize;
	private final boolean allowMissingOriginalId;

	private EgovEmbeddingStoreWriter(Builder builder) {
		this.embeddingStore = builder.embeddingStore;
		this.embeddingModel = builder.embeddingModel;
		this.deleteBatchSize = builder.deleteBatchSize;
		this.embedBatchSize = builder.embedBatchSize;
		this.allowMissingOriginalId = builder.allowMissingOriginalId;
	}

	/**
	 * 빌더.
	 *
	 * @param embeddingStore 대상 저장소
	 * @param embeddingModel 임베딩 모델
	 * @return 빌더
	 */
	public static Builder builder(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
		return new Builder(embeddingStore, embeddingModel);
	}

	/**
	 * 기본 옵션의 라이터.
	 *
	 * @param embeddingStore 대상 저장소
	 * @param embeddingModel 임베딩 모델
	 * @return 라이터
	 */
	public static EgovEmbeddingStoreWriter of(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
		return builder(embeddingStore, embeddingModel).build();
	}

	/**
	 * stale 세그먼트를 지운 뒤 id 를 정해 임베딩·저장한다.
	 *
	 * @param segments 세그먼트(빈 목록은 저장소·모델을 호출하지 않는다)
	 * @return 저장된 세그먼트 id(입력 순서)
	 * @throws IllegalArgumentException {@code original_id} 가 없는 세그먼트가 있고 허용 옵션이 꺼져 있을 때(호출 전에 검사)
	 */
	public List<String> write(List<TextSegment> segments) {
		Objects.requireNonNull(segments, "segments must not be null");
		if (segments.isEmpty()) {
			return List.of();
		}
		Set<String> originalIds = new LinkedHashSet<>();
		int missing = 0;
		for (TextSegment segment : segments) {
			Metadata metadata = segment.metadata();
			String originalId = metadata == null ? null : metadata.getString(EgovDocumentMetadata.ORIGINAL_ID);
			if (originalId == null || originalId.isBlank()) {
				if (!allowMissingOriginalId) {
					throw new IllegalArgumentException("세그먼트에 " + EgovDocumentMetadata.ORIGINAL_ID
							+ " 메타데이터가 없어 stale 세그먼트를 정리할 수 없습니다. 로더·분할기가 original_id 를 넣게 하거나, 정리 없이 저장하려면 "
							+ "allowMissingOriginalId(true) 를 명시하세요");
				}
				missing++;
			} else {
				originalIds.add(originalId);
			}
		}
		if (missing > 0) {
			LOG.warn("{} 없는 세그먼트 {}건 — stale 정리 없이 저장합니다", EgovDocumentMetadata.ORIGINAL_ID, missing);
		}
		List<String> ids = new ArrayList<>(originalIds);
		for (int from = 0; from < ids.size(); from += deleteBatchSize) {
			List<String> batch = ids.subList(from, Math.min(from + deleteBatchSize, ids.size()));
			embeddingStore.removeAll(MetadataFilterBuilder.metadataKey(EgovDocumentMetadata.ORIGINAL_ID).isIn(new ArrayList<>(batch)));
		}
		List<String> stored = new ArrayList<>(segments.size());
		for (int from = 0; from < segments.size(); from += embedBatchSize) {
			List<TextSegment> batch = segments.subList(from, Math.min(from + embedBatchSize, segments.size()));
			List<String> batchIds = new ArrayList<>(batch.size());
			List<TextSegment> withIds = new ArrayList<>(batch.size());
			for (TextSegment segment : batch) {
				String id = UUID.randomUUID().toString();
				Metadata metadata = segment.metadata() == null ? new Metadata() : segment.metadata().copy();
				metadata.put(EgovLangChain4jMetadata.SEGMENT_ID, id);
				batchIds.add(id);
				withIds.add(new TextSegment(segment.text(), metadata));
			}
			List<Embedding> embeddings = embeddingModel.embedAll(withIds).content();
			if (embeddings == null || embeddings.size() != withIds.size()) {
				throw new IllegalStateException("임베딩 모델이 세그먼트 수(" + withIds.size() + ")와 다른 개수("
						+ (embeddings == null ? 0 : embeddings.size()) + ")의 벡터를 돌려주었습니다");
			}
			embeddingStore.addAll(batchIds, embeddings, withIds);
			stored.addAll(batchIds);
		}
		return List.copyOf(stored);
	}

	/** {@link EgovEmbeddingStoreWriter} 빌더. */
	public static final class Builder {
		private final EmbeddingStore<TextSegment> embeddingStore;
		private final EmbeddingModel embeddingModel;
		private int deleteBatchSize = DEFAULT_DELETE_BATCH_SIZE;
		private int embedBatchSize = DEFAULT_EMBED_BATCH_SIZE;
		private boolean allowMissingOriginalId = false;

		private Builder(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
			this.embeddingStore = Objects.requireNonNull(embeddingStore, "embeddingStore must not be null");
			this.embeddingModel = Objects.requireNonNull(embeddingModel, "embeddingModel must not be null");
		}

		/** 삭제 IN 목록 배치 크기(1 이상, 기본 {@value #DEFAULT_DELETE_BATCH_SIZE}). */
		public Builder deleteBatchSize(int deleteBatchSize) {
			this.deleteBatchSize = deleteBatchSize;
			return this;
		}

		/** 임베딩·추가 배치 크기(1 이상, 기본 {@value #DEFAULT_EMBED_BATCH_SIZE}). */
		public Builder embedBatchSize(int embedBatchSize) {
			this.embedBatchSize = embedBatchSize;
			return this;
		}

		/** {@code original_id} 없는 세그먼트를 정리 없이 저장할지(기본 false — 예외). */
		public Builder allowMissingOriginalId(boolean allow) {
			this.allowMissingOriginalId = allow;
			return this;
		}

		public EgovEmbeddingStoreWriter build() {
			if (deleteBatchSize < 1) {
				throw new IllegalArgumentException("deleteBatchSize 는 1 이상이어야 합니다: " + deleteBatchSize);
			}
			if (embedBatchSize < 1) {
				throw new IllegalArgumentException("embedBatchSize 는 1 이상이어야 합니다: " + embedBatchSize);
			}
			return new EgovEmbeddingStoreWriter(this);
		}
	}
}
