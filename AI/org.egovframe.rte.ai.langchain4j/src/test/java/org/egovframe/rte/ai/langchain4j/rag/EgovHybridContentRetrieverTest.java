package org.egovframe.rte.ai.langchain4j.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.egovframe.rte.ai.core.retrieval.EgovLexicalSearch;
import org.egovframe.rte.ai.langchain4j.support.FakeEmbeddingModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

/**
 * 하이브리드 검색기 계약 테스트 — RRF 융합·EMBEDDING_ID 식별자·공백·어휘 실패 전파·어휘 단독 포함/재정렬 전용·SPI 필수
 * (스펙 ai/langchain4j-hybrid-retrieval 1·2항).
 */
class EgovHybridContentRetrieverTest {

	private static Content content(String id) {
		Map<ContentMetadata, Object> metadata = new EnumMap<>(ContentMetadata.class);
		metadata.put(ContentMetadata.EMBEDDING_ID, id);
		metadata.put(ContentMetadata.SCORE, 0.5);
		return Content.from(TextSegment.from("본문 " + id), metadata);
	}

	private static ContentRetriever dense(String... ids) {
		return query -> List.of(ids).stream().map(EgovHybridContentRetrieverTest::content).toList();
	}

	private static EgovLexicalSearch lexical(String... ids) {
		return (query, topK) -> List.of(ids);
	}

	private static EgovSegmentLookup lookup(String... ids) {
		return requested -> requested.stream().filter(List.of(ids)::contains)
				.collect(Collectors.toMap(id -> id, id -> TextSegment.from("본문 " + id)));
	}

	private static String idOf(Content content) {
		return String.valueOf(content.metadata().get(ContentMetadata.EMBEDDING_ID));
	}

	@Test
	@DisplayName("두 채널에 모두 있는 세그먼트가 첫 번째이고 결과는 topK 건, SCORE 는 내림차순 RRF 점수다")
	void fusesInRrfOrder() {
		EgovHybridContentRetriever retriever = EgovHybridContentRetriever.builder()
				.dense(dense("A", "B", "C")).lexical(lexical("C", "D")).segmentLookup(lookup("D")).topK(3).build();

		List<Content> result = retriever.retrieve(new Query("페이징 설정"));

		assertEquals(3, result.size());
		assertEquals("C", idOf(result.get(0)));
		assertEquals(1.0 / 62 + 1.0 / 60, (Double) result.get(0).metadata().get(ContentMetadata.SCORE), 1e-12);
		for (int i = 1; i < result.size(); i++) {
			assertTrue((Double) result.get(i - 1).metadata().get(ContentMetadata.SCORE) >= (Double) result.get(i).metadata().get(ContentMetadata.SCORE));
		}
	}

	@Test
	@DisplayName("공백 질의는 예외이고 두 채널 모두 호출되지 않는다")
	void rejectsBlankQuery() {
		AtomicInteger denseCalls = new AtomicInteger();
		AtomicInteger lexicalCalls = new AtomicInteger();
		EgovHybridContentRetriever retriever = EgovHybridContentRetriever.builder()
				.dense(query -> {
					denseCalls.incrementAndGet();
					return List.of();
				})
				.lexical((query, topK) -> {
					lexicalCalls.incrementAndGet();
					return List.of();
				}).rerankOnly().build();

		assertThrows(IllegalArgumentException.class, () -> retriever.retrieve(new Query("   ")));
		assertEquals(0, denseCalls.get());
		assertEquals(0, lexicalCalls.get());
	}

	@Test
	@DisplayName("어휘 채널의 예외는 그대로 전파된다")
	void propagatesLexicalFailure() {
		EgovHybridContentRetriever retriever = EgovHybridContentRetriever.builder().dense(dense("A"))
				.lexical((query, topK) -> {
					throw new IllegalStateException("lexical down");
				}).rerankOnly().build();

		assertEquals("lexical down", assertThrows(IllegalStateException.class, () -> retriever.retrieve(new Query("질의"))).getMessage());
	}

	@Test
	@DisplayName("어휘 채널에만 있는 세그먼트는 조회 SPI 로 본문을 가져와 포함한다")
	void includesLexicalOnly() {
		EgovHybridContentRetriever retriever = EgovHybridContentRetriever.builder()
				.dense(dense("A", "B")).lexical(lexical("D", "A")).segmentLookup(lookup("D")).build();

		List<Content> result = retriever.retrieve(new Query("질의"));

		assertEquals(List.of("A", "D", "B"), result.stream().map(EgovHybridContentRetrieverTest::idOf).toList());
		assertEquals("본문 D", result.get(1).textSegment().text());
	}

	@Test
	@DisplayName("재정렬 전용은 어휘 단독 세그먼트를 제외하고, 조회 SPI 가 못 찾은 식별자도 빠진다")
	void rerankOnlyAndMissingLookup() {
		EgovHybridContentRetriever rerank = EgovHybridContentRetriever.builder()
				.dense(dense("A", "B")).lexical(lexical("D", "A")).rerankOnly().build();
		assertEquals(List.of("A", "B"), rerank.retrieve(new Query("질의")).stream().map(EgovHybridContentRetrieverTest::idOf).toList());
		assertTrue(rerank.rerankOnly());

		EgovHybridContentRetriever missing = EgovHybridContentRetriever.builder()
				.dense(dense("A")).lexical(lexical("Z")).segmentLookup(lookup()).build();
		assertEquals(List.of("A"), missing.retrieve(new Query("질의")).stream().map(EgovHybridContentRetrieverTest::idOf).toList());
	}

	@Test
	@DisplayName("조회 SPI 도 재정렬 전용 선택도 없으면 생성 시 예외다")
	void requiresLookupOrRerankOnly() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> EgovHybridContentRetriever.builder().dense(dense("A")).lexical(lexical("A")).build());
		assertTrue(e.getMessage().contains("rerankOnly"), e.getMessage());
		assertThrows(IllegalArgumentException.class, () -> EgovHybridContentRetriever.builder()
				.dense(dense("A")).lexical(lexical("A")).segmentLookup(lookup("A")).rerankOnly().build());
	}

	@Test
	@DisplayName("EmbeddingStoreContentRetriever 가 준 EMBEDDING_ID 로 어휘 채널 식별자와 융합된다")
	void worksWithEmbeddingStoreContentRetriever() {
		InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
		FakeEmbeddingModel model = new FakeEmbeddingModel();
		List<TextSegment> segments = List.of(TextSegment.from("전자정부 표준프레임워크 소개"), TextSegment.from("페이징 설정 방법 PaginationInfo"),
				TextSegment.from("배치 작업 실행 절차"));
		List<Embedding> embeddings = model.embedAll(segments).content();
		store.addAll(List.of("A", "B", "C"), embeddings, segments);
		ContentRetriever dense = EmbeddingStoreContentRetriever.builder().embeddingStore(store).embeddingModel(model).maxResults(3).minScore(0.0).build();

		List<Content> result = EgovHybridContentRetriever.builder().dense(dense).lexical(lexical("C")).rerankOnly().topK(3).build()
				.retrieve(new Query("페이징 설정 방법 PaginationInfo"));

		assertEquals(3, result.size());
		assertNotNull(result.get(0).metadata().get(ContentMetadata.SCORE));
		assertTrue(result.stream().map(EgovHybridContentRetrieverTest::idOf).toList().containsAll(List.of("A", "B", "C")));
		assertEquals("C", idOf(result.get(0)), "밀집 3위 + 어휘 1위 → RRF 1위");
	}
}
