package org.egovframe.rte.ai.langchain4j.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.langchain4j.support.FakeEmbeddingModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

/**
 * 재색인 라이터 계약 테스트 — InMemoryEmbeddingStore(필터 삭제 실동작)로 stale 정리·id 일치·배치·거부(스펙 ai/langchain4j-document-etl 4항).
 */
class EgovEmbeddingStoreWriterTest {

	private static TextSegment segment(String text, String originalId) {
		return new TextSegment(text, new Metadata().put(EgovDocumentMetadata.ORIGINAL_ID, originalId));
	}

	private static List<EmbeddingMatch<TextSegment>> all(InMemoryEmbeddingStore<TextSegment> store) {
		return store.search(EmbeddingSearchRequest.builder()
				.queryEmbedding(Embedding.from(FakeEmbeddingModel.vector("질의")))
				.maxResults(100).minScore(0.0).build()).matches();
	}

	@Test
	@DisplayName("재색인 — 같은 original_id 의 기존 세그먼트 3건이 지워지고 새 세그먼트 2건만 남으며 메타데이터 id 가 저장소 id 와 같다")
	void reindexReplacesStaleSegments() {
		InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
		FakeEmbeddingModel model = new FakeEmbeddingModel();
		EgovEmbeddingStoreWriter writer = EgovEmbeddingStoreWriter.of(store, model);
		writer.write(List.of(segment("옛 1", "doc-a"), segment("옛 2", "doc-a"), segment("옛 3", "doc-a"), segment("유지", "doc-b")));

		List<String> ids = writer.write(List.of(segment("새 1", "doc-a"), segment("새 2", "doc-a")));

		List<EmbeddingMatch<TextSegment>> matches = all(store);
		assertEquals(3, matches.size(), "doc-a 2 + doc-b 1");
		long docA = matches.stream().filter(m -> "doc-a".equals(m.embedded().metadata().getString(EgovDocumentMetadata.ORIGINAL_ID))).count();
		assertEquals(2, docA);
		for (EmbeddingMatch<TextSegment> match : matches) {
			assertEquals(match.embeddingId(), match.embedded().metadata().getString(EgovLangChain4jMetadata.SEGMENT_ID), "저장소 id = 메타데이터 id");
		}
		assertEquals(2, ids.size());
		assertTrue(matches.stream().map(EmbeddingMatch::embeddingId).toList().containsAll(ids));
	}

	@Test
	@DisplayName("original_id 없는 세그먼트는 예외이고 모델·저장소는 호출되지 않는다")
	void rejectsMissingOriginalId() {
		InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
		FakeEmbeddingModel model = new FakeEmbeddingModel();

		assertThrows(IllegalArgumentException.class,
				() -> EgovEmbeddingStoreWriter.of(store, model).write(List.of(segment("ok", "doc-a"), TextSegment.from("no origin"))));

		assertEquals(0, model.calls());
		assertTrue(all(store).isEmpty());
	}

	@Test
	@DisplayName("허용 옵션을 켜면 original_id 없는 세그먼트도 정리 없이 저장된다")
	void allowsMissingWhenOptedIn() {
		InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

		List<String> ids = EgovEmbeddingStoreWriter.builder(store, new FakeEmbeddingModel()).allowMissingOriginalId(true).build()
				.write(List.of(TextSegment.from("no origin")));

		assertEquals(1, ids.size());
		assertEquals(1, all(store).size());
	}

	@Test
	@DisplayName("배치 크기 2 — 5건은 임베딩 호출 3회(2·2·1)로 저장된다")
	void splitsBatches() {
		InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
		FakeEmbeddingModel model = new FakeEmbeddingModel();

		List<String> ids = EgovEmbeddingStoreWriter.builder(store, model).deleteBatchSize(2).embedBatchSize(2).build()
				.write(List.of(segment("1", "o1"), segment("2", "o2"), segment("3", "o3"), segment("4", "o4"), segment("5", "o5")));

		assertEquals(3, model.calls());
		assertEquals(5, ids.size());
		assertEquals(5, all(store).size());
	}

	@Test
	@DisplayName("빈 목록은 모델·저장소를 호출하지 않고 배치 크기는 1 이상이어야 한다")
	void emptyAndValidation() {
		InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
		FakeEmbeddingModel model = new FakeEmbeddingModel();

		assertTrue(EgovEmbeddingStoreWriter.of(store, model).write(List.of()).isEmpty());
		assertEquals(0, model.calls());
		assertThrows(IllegalArgumentException.class, () -> EgovEmbeddingStoreWriter.builder(store, model).embedBatchSize(0).build());
		assertThrows(IllegalArgumentException.class, () -> EgovEmbeddingStoreWriter.builder(store, model).deleteBatchSize(0).build());
	}
}
