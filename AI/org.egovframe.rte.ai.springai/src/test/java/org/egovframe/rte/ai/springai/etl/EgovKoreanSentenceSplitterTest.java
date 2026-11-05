package org.egovframe.rte.ai.springai.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.core.text.EgovSentenceChunker;
import org.egovframe.rte.ai.springai.support.LogCapture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

/**
 * 문장 청킹 분할기 계약 테스트 — 기반 청킹기와 동일 결과·메타데이터·한도 초과 정책(스펙 ai/springai-document-etl 2항,
 * ai/springai-adapter 2·3항).
 */
class EgovKoreanSentenceSplitterTest {

	private static final String TEXT = String.join(" ",
			"전자정부 표준프레임워크는 공공 정보화 사업의 개발 기반이다.",
			"실행환경은 공통 기반 모듈을 제공한다.",
			"개발환경은 도구를 제공한다.",
			"운영환경은 배포와 모니터링을 돕는다.",
			"관리환경은 조직의 표준을 관리한다.",
			"페이징은 PaginationInfo 로 설정한다.",
			"보안은 스프링 시큐리티를 확장한다.",
			"배치는 스프링 배치를 기반으로 한다.",
			"AI 모듈은 문서 검색과 응답 생성을 돕는다.",
			"이 문장은 마지막 문장이다.");

	private static Document document(String id, String text) {
		return Document.builder().id(id).text(text)
				.metadata(Map.of(EgovDocumentMetadata.ORIGINAL_ID, id, EgovDocumentMetadata.FILE_NAME, id + ".md"))
				.build();
	}

	@Test
	@DisplayName("청크 텍스트는 같은 계산기·한도의 기반 청킹기 결과와 순서까지 같다")
	void sameTextsAsCoreChunker() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder(EgovTokenSizers.chars()).maxSize(60).build();
		EgovSentenceChunker chunker = EgovSentenceChunker.builder(String::length).maxSize(60).build();

		List<String> viaSplitter = splitter.apply(List.of(document("d", TEXT))).stream().map(Document::getText).toList();

		assertEquals(chunker.chunk(TEXT).texts(), viaSplitter);
		assertTrue(viaSplitter.size() >= 4, "여러 청크로 나뉜다: " + viaSplitter.size());
	}

	@Test
	@DisplayName("청크는 원본 메타데이터를 전부 복사하고 chunk_index·chunk_count 를 갖는다")
	void copiesMetadataAndNumbersChunks() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder(EgovTokenSizers.chars()).maxSize(60).build();

		List<Document> chunks = splitter.apply(List.of(document("doc-a", TEXT)));

		int count = chunks.size();
		for (int i = 0; i < count; i++) {
			Map<String, Object> metadata = chunks.get(i).getMetadata();
			assertEquals("doc-a", metadata.get(EgovDocumentMetadata.ORIGINAL_ID));
			assertEquals("doc-a.md", metadata.get(EgovDocumentMetadata.FILE_NAME));
			assertEquals(i, metadata.get(EgovSpringAiMetadata.CHUNK_INDEX));
			assertEquals(count, metadata.get(EgovSpringAiMetadata.CHUNK_COUNT));
			assertFalse(metadata.containsKey(EgovSpringAiMetadata.TRUNCATED));
			assertFalse(chunks.get(i).getId().equals("doc-a"), "청크 id 는 새 값");
		}
	}

	@Test
	@DisplayName("한도 초과 기본 정책(FAIL)은 문서 id 를 담은 예외를 던진다")
	void failPolicyThrows() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder(EgovTokenSizers.chars())
				.maxSize(60).maxChunks(2).build();

		EgovChunkLimitException e = assertThrows(EgovChunkLimitException.class,
				() -> splitter.apply(List.of(document("doc-a", TEXT))));

		assertEquals("doc-a", e.documentId());
		assertEquals(2, e.maxChunks());
		assertTrue(e.getMessage().contains("doc-a"));
	}

	@Test
	@DisplayName("한도 초과 표식 정책(MARK)은 모든 청크에 truncated=true 를 붙이고 경고를 남긴다")
	void markPolicyFlagsChunks() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder(EgovTokenSizers.chars())
				.maxSize(60).maxChunks(2).onTruncation(EgovTruncationPolicy.MARK).build();

		try (LogCapture log = LogCapture.of(EgovKoreanSentenceSplitter.class)) {
			List<Document> chunks = splitter.apply(List.of(document("doc-a", TEXT)));

			assertEquals(2, chunks.size());
			for (Document chunk : chunks) {
				assertEquals(Boolean.TRUE, chunk.getMetadata().get(EgovSpringAiMetadata.TRUNCATED));
			}
			assertEquals(1, log.lines().size());
			assertTrue(log.lines().get(0).contains("doc-a"), log.lines().get(0));
		}
	}

	@Test
	@DisplayName("문자 수 계산기로 바꾸면 모든 청크가 지정 문자 수 이하다")
	void charSizerBoundsChunkLength() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder(String::length).maxSize(100).build();

		for (Document chunk : splitter.apply(List.of(document("d", TEXT)))) {
			assertTrue(chunk.getText().length() <= 100, chunk.getText());
		}
	}

	@Test
	@DisplayName("텍스트 단독 분할 경로(splitText)에서는 표식을 붙일 수 없어 잘림을 예외로 알린다")
	void splitTextPathSignalsTruncation() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder(EgovTokenSizers.chars())
				.maxSize(60).maxChunks(2).onTruncation(EgovTruncationPolicy.MARK).build();

		assertThrows(EgovChunkLimitException.class, () -> splitter.splitText(TEXT));
		assertEquals(2, splitter.split(Document.builder().id("x").text(TEXT).build()).size(), "split(Document) 는 apply 경로 — MARK 정책 적용");
	}

	@Test
	@DisplayName("기본 빌더는 cl100k 토큰 계산기와 기본 크기를 쓴다")
	void defaultBuilderUsesTokens() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder().build();

		assertEquals(EgovKoreanSentenceSplitter.DEFAULT_MAX_SIZE, splitter.chunker().maxSize());
		assertEquals(EgovTruncationPolicy.FAIL, splitter.truncationPolicy());
		List<Document> chunks = splitter.apply(List.of(document("d", TEXT)));
		assertEquals(1, chunks.size(), "짧은 본문은 토큰 한도 안에서 청크 1개");
	}
}
