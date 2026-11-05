package org.egovframe.rte.ai.springai.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.egovframe.rte.ai.core.retrieval.EgovLexicalSearch;
import org.egovframe.rte.ai.springai.support.FakeEmbeddingModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;

/**
 * 하이브리드 검색기 계약 테스트 — RRF 융합 순서·공백 질의·어휘 채널 실패 전파·어휘 단독 문서 포함/재정렬 전용·조회 SPI 필수
 * (스펙 ai/springai-hybrid-retrieval 1·2항).
 */
class EgovHybridDocumentRetrieverTest {

	private static Document doc(String id) {
		return Document.builder().id(id).text("본문 " + id).build();
	}

	private static DocumentRetriever dense(String... ids) {
		return query -> List.of(ids).stream().map(EgovHybridDocumentRetrieverTest::doc).toList();
	}

	private static EgovLexicalSearch lexical(String... ids) {
		return (query, topK) -> List.of(ids);
	}

	private static EgovDocumentLookup lookup(String... ids) {
		Map<String, Document> store = List.of(ids).stream().collect(java.util.stream.Collectors.toMap(id -> id, EgovHybridDocumentRetrieverTest::doc));
		return requested -> requested.stream().map(store::get).filter(d -> d != null).toList();
	}

	@Test
	@DisplayName("두 채널에 모두 있는 문서가 첫 번째이고 결과는 topK 건, score 는 내림차순 RRF 점수다")
	void fusesInRrfOrder() {
		EgovHybridDocumentRetriever retriever = EgovHybridDocumentRetriever.builder()
				.dense(dense("A", "B", "C")).lexical(lexical("C", "D")).documentLookup(lookup("D")).topK(3).build();

		List<Document> result = retriever.retrieve(new Query("페이징 설정"));

		assertEquals(3, result.size());
		assertEquals("C", result.get(0).getId());
		assertEquals(1.0 / 62 + 1.0 / 60, result.get(0).getScore(), 1e-12, "양 채널 RRF 합");
		for (int i = 1; i < result.size(); i++) {
			assertTrue(result.get(i - 1).getScore() >= result.get(i).getScore(), "내림차순");
		}
	}

	@Test
	@DisplayName("공백 질의는 예외이고 두 채널 모두 호출되지 않는다")
	void rejectsBlankQuery() {
		AtomicInteger denseCalls = new AtomicInteger();
		AtomicInteger lexicalCalls = new AtomicInteger();
		EgovHybridDocumentRetriever retriever = EgovHybridDocumentRetriever.builder()
				.dense(query -> {
					denseCalls.incrementAndGet();
					return List.of();
				})
				.lexical((query, topK) -> {
					lexicalCalls.incrementAndGet();
					return List.of();
				})
				.rerankOnly().build();

		assertThrows(IllegalArgumentException.class, () -> retriever.retrieve(new Query("   ")));
		assertEquals(0, denseCalls.get());
		assertEquals(0, lexicalCalls.get());
	}

	@Test
	@DisplayName("어휘 채널의 예외는 그대로 전파된다")
	void propagatesLexicalFailure() {
		EgovHybridDocumentRetriever retriever = EgovHybridDocumentRetriever.builder()
				.dense(dense("A")).lexical((query, topK) -> {
					throw new IllegalStateException("lexical down");
				}).rerankOnly().build();

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> retriever.retrieve(new Query("질의")));
		assertEquals("lexical down", e.getMessage());
	}

	@Test
	@DisplayName("어휘 채널에만 있는 문서는 조회 SPI 로 본문을 가져와 포함한다")
	void includesLexicalOnlyDocuments() {
		EgovHybridDocumentRetriever retriever = EgovHybridDocumentRetriever.builder()
				.dense(dense("A", "B")).lexical(lexical("D", "A")).documentLookup(lookup("D")).build();

		List<Document> result = retriever.retrieve(new Query("질의"));

		assertEquals(List.of("A", "D", "B"), result.stream().map(Document::getId).toList());
		assertEquals("본문 D", result.get(1).getText());
	}

	@Test
	@DisplayName("재정렬 전용은 어휘 단독 문서를 제외한다")
	void rerankOnlyExcludesLexicalOnly() {
		EgovHybridDocumentRetriever retriever = EgovHybridDocumentRetriever.builder()
				.dense(dense("A", "B")).lexical(lexical("D", "A")).rerankOnly().build();

		List<Document> result = retriever.retrieve(new Query("질의"));

		assertEquals(List.of("A", "B"), result.stream().map(Document::getId).toList());
		assertTrue(retriever.rerankOnly());
	}

	@Test
	@DisplayName("조회 SPI 도 재정렬 전용 선택도 없으면 생성 시 예외다")
	void requiresLookupOrExplicitRerankOnly() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> EgovHybridDocumentRetriever.builder().dense(dense("A")).lexical(lexical("A")).build());
		assertTrue(e.getMessage().contains("rerankOnly"), e.getMessage());
		assertThrows(IllegalArgumentException.class, () -> EgovHybridDocumentRetriever.builder()
				.dense(dense("A")).lexical(lexical("A")).documentLookup(lookup("A")).rerankOnly().build());
	}

	@Test
	@DisplayName("조회 SPI 가 돌려주지 않은 id 는 결과에서 빠진다")
	void skipsIdsLookupCannotFind() {
		EgovHybridDocumentRetriever retriever = EgovHybridDocumentRetriever.builder()
				.dense(dense("A")).lexical(lexical("Z")).documentLookup(lookup()).build();

		List<Document> result = retriever.retrieve(new Query("질의"));

		assertEquals(List.of("A"), result.stream().map(Document::getId).toList());
	}

	@Test
	@DisplayName("Spring AI SimpleVectorStore + VectorStoreDocumentRetriever 를 밀집 채널로 그대로 쓸 수 있다")
	void worksWithSpringAiVectorStore() {
		SimpleVectorStore store = SimpleVectorStore.builder(new FakeEmbeddingModel()).build();
		store.add(List.of(
				Document.builder().id("A").text("전자정부 표준프레임워크 소개").build(),
				Document.builder().id("B").text("페이징 설정 방법 PaginationInfo").build(),
				Document.builder().id("C").text("배치 작업 실행 절차").build()));
		DocumentRetriever dense = VectorStoreDocumentRetriever.builder().vectorStore(store).topK(3).similarityThreshold(0.0).build();
		EgovHybridDocumentRetriever retriever = EgovHybridDocumentRetriever.builder()
				.dense(dense).lexical(lexical("C")).rerankOnly().topK(3).build();

		List<Document> result = retriever.retrieve(new Query("페이징 설정 방법 PaginationInfo"));

		assertEquals(3, result.size());
		assertNotNull(result.get(0).getScore());
		assertFalse(result.stream().map(Document::getId).toList().contains("Z"));
		assertTrue(result.stream().map(Document::getId).toList().containsAll(List.of("A", "B", "C")));
	}
}
