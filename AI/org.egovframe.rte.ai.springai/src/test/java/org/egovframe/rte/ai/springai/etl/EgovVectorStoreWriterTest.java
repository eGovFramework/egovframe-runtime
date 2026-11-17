package org.egovframe.rte.ai.springai.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.springai.support.RecordingVectorStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

/**
 * 재색인 라이터 계약 테스트 — stale 청크 IN 삭제 후 저장·original_id 검사·배치 분할(스펙 ai/springai-document-etl 5항).
 */
class EgovVectorStoreWriterTest {

	private static Document chunk(String id, String originalId) {
		return Document.builder().id(id).text("청크 " + id)
				.metadata(Map.of(EgovDocumentMetadata.ORIGINAL_ID, originalId)).build();
	}

	@Test
	@DisplayName("재색인 — 같은 original_id 의 기존 청크 3건이 IN 필터로 지워지고 새 청크 2건만 남는다")
	void deletesStaleChunksThenAdds() {
		RecordingVectorStore store = new RecordingVectorStore();
		store.add(List.of(chunk("old-1", "doc-a"), chunk("old-2", "doc-a"), chunk("old-3", "doc-a"), chunk("keep", "doc-b")));
		store.addBatches().clear();

		EgovVectorStoreWriter.of(store).accept(List.of(chunk("new-1", "doc-a"), chunk("new-2", "doc-a")));

		assertEquals(1, store.deleteFilters().size());
		Filter.Expression filter = store.deleteFilters().get(0);
		assertEquals(Filter.ExpressionType.IN, filter.type());
		assertEquals(new Filter.Key(EgovDocumentMetadata.ORIGINAL_ID), filter.left());
		assertEquals(List.of("doc-a"), ((Filter.Value) filter.right()).value());
		assertEquals(1, store.addBatches().size());
		assertEquals(List.of("keep", "new-1", "new-2"), new ArrayList<>(store.documents().keySet()));
	}

	@Test
	@DisplayName("original_id 없는 문서는 예외이고 저장소는 호출되지 않는다")
	void rejectsMissingOriginalId() {
		RecordingVectorStore store = new RecordingVectorStore();
		List<Document> documents = List.of(chunk("ok", "doc-a"), Document.builder().id("no-origin").text("x").build());

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> EgovVectorStoreWriter.of(store).accept(documents));

		assertTrue(e.getMessage().contains("no-origin"), e.getMessage());
		assertEquals(0, store.calls());
	}

	@Test
	@DisplayName("허용 옵션을 켜면 original_id 없는 문서는 stale 정리 없이 저장된다")
	void allowsMissingOriginalIdWhenOptedIn() {
		RecordingVectorStore store = new RecordingVectorStore();

		EgovVectorStoreWriter.builder(store).allowMissingOriginalId(true).build()
				.accept(List.of(Document.builder().id("no-origin").text("x").build()));

		assertTrue(store.deleteFilters().isEmpty());
		assertEquals(1, store.addBatches().size());
		assertTrue(store.documents().containsKey("no-origin"));
	}

	@Test
	@DisplayName("배치 크기 2 — 원본 5건은 삭제 필터 3회(2·2·1), 추가 3회로 나뉜다")
	void splitsBatches() {
		RecordingVectorStore store = new RecordingVectorStore();
		List<Document> documents = List.of(chunk("c1", "o1"), chunk("c2", "o2"), chunk("c3", "o3"), chunk("c4", "o4"), chunk("c5", "o5"));

		EgovVectorStoreWriter.builder(store).deleteBatchSize(2).addBatchSize(2).build().accept(documents);

		assertEquals(3, store.deleteFilters().size());
		assertEquals(List.of(2, 2, 1), store.deleteFilters().stream()
				.map(f -> ((List<?>) ((Filter.Value) f.right()).value()).size()).toList());
		assertEquals(List.of(2, 2, 1), store.addBatches().stream().map(List::size).toList());
		assertEquals(5, store.documents().size());
	}

	@Test
	@DisplayName("빈 목록은 저장소를 호출하지 않는다")
	void emptyListIsNoop() {
		RecordingVectorStore store = new RecordingVectorStore();

		EgovVectorStoreWriter.of(store).accept(List.of());

		assertEquals(0, store.calls());
	}

	@Test
	@DisplayName("배치 크기는 1 이상이어야 한다")
	void validatesBatchSizes() {
		RecordingVectorStore store = new RecordingVectorStore();
		assertThrows(IllegalArgumentException.class, () -> EgovVectorStoreWriter.builder(store).deleteBatchSize(0).build());
		assertThrows(IllegalArgumentException.class, () -> EgovVectorStoreWriter.builder(store).addBatchSize(0).build());
	}

	@Test
	@DisplayName("필터 삭제 미지원 저장소(SimpleVectorStore 등)는 안내 예외로 감싼다 — 원인 예외 보존")
	void wrapsUnsupportedFilterDelete() {
		VectorStore unsupported = new VectorStore() {
			@Override
			public void add(List<Document> documents) {
			}

			@Override
			public void delete(List<String> idList) {
			}

			@Override
			public void delete(Filter.Expression filterExpression) {
				throw new UnsupportedOperationException();
			}

			@Override
			public List<Document> similaritySearch(SearchRequest request) {
				return List.of();
			}
		};
		EgovVectorStoreWriter writer = EgovVectorStoreWriter.of(unsupported);
		Document chunk = new Document("c1", "본문", Map.of(EgovDocumentMetadata.ORIGINAL_ID, "doc-1"));
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> writer.accept(List.of(chunk)));
		assertTrue(e.getMessage().contains("필터 삭제"));
		assertTrue(e.getCause() instanceof UnsupportedOperationException);
	}
}
