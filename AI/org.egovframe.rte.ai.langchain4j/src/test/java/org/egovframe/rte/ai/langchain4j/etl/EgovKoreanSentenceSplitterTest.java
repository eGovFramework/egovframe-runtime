package org.egovframe.rte.ai.langchain4j.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.core.text.EgovSentenceChunker;
import org.egovframe.rte.ai.langchain4j.support.LogCapture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;

/**
 * 문장 청킹 분할기 계약 테스트 — 기반 청킹기와 동일 결과·메타데이터·한도 초과 정책·추정기 교체(스펙 ai/langchain4j-document-etl 2항,
 * ai/langchain4j-adapter 2·3항).
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

	private static Document document(String originalId, String text) {
		Metadata metadata = new Metadata().put(EgovDocumentMetadata.ORIGINAL_ID, originalId)
				.put(EgovDocumentMetadata.FILE_NAME, originalId + ".md");
		return Document.from(text, metadata);
	}

	@Test
	@DisplayName("세그먼트 텍스트는 같은 계산기·한도의 기반 청킹기 결과와 순서까지 같다")
	void sameTextsAsCoreChunker() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder().maxSize(60).build();
		EgovSentenceChunker chunker = EgovSentenceChunker.builder(String::length).maxSize(60).build();

		List<String> viaSplitter = splitter.split(document("d", TEXT)).stream().map(TextSegment::text).toList();

		assertEquals(chunker.chunk(TEXT).texts(), viaSplitter);
		assertTrue(viaSplitter.size() >= 4, "여러 세그먼트로 나뉜다: " + viaSplitter.size());
	}

	@Test
	@DisplayName("세그먼트는 원본 메타데이터를 복사하고 chunk_index·chunk_count 를 갖는다")
	void copiesMetadata() {
		List<TextSegment> segments = EgovKoreanSentenceSplitter.builder().maxSize(60).build().split(document("doc-a", TEXT));

		int count = segments.size();
		for (int i = 0; i < count; i++) {
			Metadata metadata = segments.get(i).metadata();
			assertEquals("doc-a", metadata.getString(EgovDocumentMetadata.ORIGINAL_ID));
			assertEquals("doc-a.md", metadata.getString(EgovDocumentMetadata.FILE_NAME));
			assertEquals(i, metadata.getInteger(EgovLangChain4jMetadata.CHUNK_INDEX));
			assertEquals(count, metadata.getInteger(EgovLangChain4jMetadata.CHUNK_COUNT));
			assertFalse(metadata.containsKey(EgovLangChain4jMetadata.TRUNCATED));
		}
	}

	@Test
	@DisplayName("한도 초과 기본 정책(FAIL)은 원본 id 를 담은 예외를 던진다")
	void failPolicyThrows() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder().maxSize(60).maxChunks(2).build();

		EgovChunkLimitException e = assertThrows(EgovChunkLimitException.class, () -> splitter.split(document("doc-a", TEXT)));

		assertEquals("doc-a", e.originalId());
		assertEquals(2, e.maxChunks());
	}

	@Test
	@DisplayName("한도 초과 표식 정책(MARK)은 모든 세그먼트에 truncated=\"true\" 를 붙이고 경고를 남긴다")
	void markPolicyFlags() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder().maxSize(60).maxChunks(2)
				.onTruncation(EgovTruncationPolicy.MARK).build();

		try (LogCapture log = LogCapture.of(EgovKoreanSentenceSplitter.class)) {
			List<TextSegment> segments = splitter.split(document("doc-a", TEXT));

			assertEquals(2, segments.size());
			for (TextSegment segment : segments) {
				assertEquals("true", segment.metadata().getString(EgovLangChain4jMetadata.TRUNCATED));
			}
			assertEquals(1, log.lines().size());
			assertTrue(log.lines().get(0).contains("doc-a"), log.lines().get(0));
		}
	}

	@Test
	@DisplayName("LangChain4j 토큰 추정기로 바꾸면 그 단위로 크기를 잰다(절반 추정기 · 한도 50 → 문자 100 이하)")
	void tokenEstimatorReplacesSizer() {
		TokenCountEstimator half = new TokenCountEstimator() {
			@Override
			public int estimateTokenCountInText(String text) {
				return text.length() / 2;
			}

			@Override
			public int estimateTokenCountInMessage(ChatMessage message) {
				return 0;
			}

			@Override
			public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
				return 0;
			}
		};

		for (TextSegment segment : EgovKoreanSentenceSplitter.builder(half).maxSize(50).build().split(document("d", TEXT))) {
			assertTrue(segment.text().length() <= 100, segment.text());
		}
	}

	@Test
	@DisplayName("기본 빌더는 문자 수 계산기·기본 크기이고 빈 문서는 세그먼트를 만들지 않는다")
	void defaults() {
		EgovKoreanSentenceSplitter splitter = EgovKoreanSentenceSplitter.builder().build();

		assertEquals(EgovKoreanSentenceSplitter.DEFAULT_MAX_SIZE, splitter.chunker().maxSize());
		assertEquals(EgovTruncationPolicy.FAIL, splitter.truncationPolicy());
		assertEquals(1, splitter.split(document("d", TEXT)).size(), "짧은 본문은 세그먼트 1개");
		Document blank = new Document() {	// LangChain4j Document.from 은 공백을 거부하므로 인터페이스로 만든다
			@Override
			public String text() {
				return "   ";
			}

			@Override
			public Metadata metadata() {
				return new Metadata();
			}
		};
		assertTrue(splitter.split(blank).isEmpty());
	}
}
