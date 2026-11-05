package org.egovframe.rte.ai.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 추출기 레지스트리 계약 테스트 — 담당 판정·미지원 형식 예외·일괄 추출의 부분 실패 보고(스펙 ai/document-text-extraction 3항).
 */
class EgovTextExtractorsTest {

	@Test
	@DisplayName("기본 5종이 등록되고 파일명으로 담당 추출기를 고른다")
	void defaultsAndLookup() {
		EgovTextExtractors extractors = EgovTextExtractors.defaults();
		assertEquals(5, extractors.extractors().size());
		assertEquals("hwp", extractors.forFile("공고.HWP").orElseThrow().type());
		assertEquals("hwpx", extractors.forFile("공고.hwpx").orElseThrow().type());
		assertEquals("docx", extractors.forFile("공고.docx").orElseThrow().type());
		assertEquals("pdf", extractors.forFile("공고.pdf").orElseThrow().type());
		assertEquals("markdown", extractors.forFile("README.md").orElseThrow().type());
		assertTrue(extractors.forFile("공고.txt").isEmpty());
		assertTrue(extractors.forFile(null).isEmpty());
	}

	@Test
	@DisplayName("일괄 추출은 손상 파일을 사유와 함께 실패 목록에 남기고 나머지를 계속 처리한다")
	void partialFailure(@TempDir Path dir) throws Exception {
		Path good1 = dir.resolve("a.md");
		Path good2 = dir.resolve("b.md");
		Path corrupt = dir.resolve("broken.hwp");
		Path empty = dir.resolve("empty.md");
		Files.writeString(good1, "# A\n본문", StandardCharsets.UTF_8);
		Files.writeString(good2, "# B\n본문", StandardCharsets.UTF_8);
		Files.write(corrupt, new byte[] { 1, 2, 3, 4, 5 });
		Files.writeString(empty, "\n", StandardCharsets.UTF_8);

		EgovExtractionResult result = EgovTextExtractors.defaults().extractAll(List.of(good1, corrupt, good2, empty));

		assertEquals(2, result.succeeded().size());
		assertEquals(List.of("md-a", "md-b"), result.succeeded().stream().map(EgovExtractedDocument::id).toList());
		assertTrue(result.hasFailures());
		assertEquals(1, result.failed().size());
		assertEquals(corrupt, result.failed().get(0).file());
		assertTrue(result.failed().get(0).reason().contains("broken.hwp"));
		assertTrue(result.failed().get(0).cause() instanceof EgovExtractionException);
		assertEquals(List.of(empty), result.empty());
	}

	@Test
	@DisplayName("미지원 형식은 예외이고 추출기 없는 레지스트리는 만들 수 없다")
	void unsupported(@TempDir Path dir) throws Exception {
		Path txt = dir.resolve("a.txt");
		Files.writeString(txt, "x", StandardCharsets.UTF_8);
		assertThrows(EgovExtractionException.class, () -> EgovTextExtractors.defaults().extract(txt));
		assertThrows(IllegalArgumentException.class, () -> EgovTextExtractors.of(List.of()));
	}

	// ---------------------------------------------------------------- 자원 예산 (R51-06, 2026-09-18)

	@Test
	@DisplayName("파일 크기 한도는 어떤 추출기든 열기 전에 적용된다 — 사용자 구현 추출기가 호출되지 않는다")
	void fileBytesBudgetAppliesBeforeAnyExtractor(@TempDir Path dir) throws Exception {
		Path big = dir.resolve("big.custom");
		Files.writeString(big, "x".repeat(2048), StandardCharsets.UTF_8);
		AtomicInteger calls = new AtomicInteger();
		EgovTextExtractor custom = new EgovTextExtractor() {
			@Override
			public String type() {
				return "custom";
			}

			@Override
			public List<String> extensions() {
				return List.of("custom");
			}

			@Override
			public List<EgovExtractedDocument> extract(Path file) {
				calls.incrementAndGet();
				return List.of(new EgovExtractedDocument("custom-x", "text", Map.of()));
			}
		};
		EgovTextExtractors extractors = EgovTextExtractors.of(List.of(custom),
				EgovExtractionBudget.builder().maxFileBytes(1024).build());

		EgovExtractionBudgetExceededException e = assertThrows(EgovExtractionBudgetExceededException.class,
				() -> extractors.extract(big));
		assertEquals(EgovExtractionBudgetExceededException.Limit.FILE_BYTES, e.getLimit());
		assertEquals(0, calls.get(), "한도를 넘는 파일은 추출기에 닿지 않아야 한다");

		Path small = dir.resolve("small.custom");
		Files.writeString(small, "x", StandardCharsets.UTF_8);
		assertEquals(1, extractors.extract(small).size());
		assertEquals(1, calls.get());
	}

	@Test
	@DisplayName("문자 수 한도 초과는 잘린 본문 없이 실패한다")
	void charsBudgetFailsWithoutPartialResult(@TempDir Path dir) throws Exception {
		Path md = dir.resolve("long.md");
		Files.writeString(md, "# 제목\n" + "가".repeat(1000), StandardCharsets.UTF_8);
		EgovTextExtractors extractors = EgovTextExtractors.defaults(EgovExtractionBudget.builder().maxChars(100).build());

		EgovExtractionBudgetExceededException e = assertThrows(EgovExtractionBudgetExceededException.class,
				() -> extractors.extract(md));
		assertEquals(EgovExtractionBudgetExceededException.Limit.CHARS, e.getLimit());
		assertEquals(100, e.getAllowed());
		assertTrue(e.getActual() > 1000, "실측 문자 수를 담는다");
		assertTrue(e.getMessage().contains("문자 수 한도"), e.getMessage());
	}

	@Test
	@DisplayName("기본 예산과 무제한은 한도 안 문서에서 같은 결과를 내고 기본값은 조회된다")
	void defaultsAndUnlimitedAgree(@TempDir Path dir) throws Exception {
		Path md = dir.resolve("note.md");
		Files.writeString(md, "# 안내\n본문이다.", StandardCharsets.UTF_8);

		assertEquals(EgovTextExtractors.defaults().extract(md),
				EgovTextExtractors.defaults(EgovExtractionBudget.unlimited()).extract(md));
		assertEquals(EgovExtractionBudget.defaults(), EgovTextExtractors.defaults().budget());
		assertEquals(EgovExtractionBudget.defaults(), EgovTextExtractors.of(List.of(new EgovMarkdownTextExtractor())).budget());
		assertEquals(EgovExtractionBudget.unlimited(), EgovTextExtractors.defaults(EgovExtractionBudget.unlimited()).budget());
	}

	@Test
	@DisplayName("일괄 추출에서 예산 초과는 사유와 함께 실패 목록에 남고 나머지는 계속 처리된다")
	void batchReportsBudgetFailure(@TempDir Path dir) throws Exception {
		Path a = dir.resolve("a.md");
		Path big = dir.resolve("big.md");
		Path b = dir.resolve("b.md");
		Files.writeString(a, "# A\n본문", StandardCharsets.UTF_8);
		Files.writeString(big, "x".repeat(2048), StandardCharsets.UTF_8);
		Files.writeString(b, "# B\n본문", StandardCharsets.UTF_8);
		EgovTextExtractors extractors = EgovTextExtractors.defaults(EgovExtractionBudget.builder().maxFileBytes(1024).build());

		EgovExtractionResult result = extractors.extractAll(List.of(a, big, b));

		assertEquals(2, result.succeeded().size());
		assertEquals(1, result.failed().size());
		assertEquals(big, result.failed().get(0).file());
		assertTrue(result.failed().get(0).reason().contains("파일 크기 한도"), result.failed().get(0).reason());
		assertTrue(result.failed().get(0).cause() instanceof EgovExtractionBudgetExceededException);
	}
}
