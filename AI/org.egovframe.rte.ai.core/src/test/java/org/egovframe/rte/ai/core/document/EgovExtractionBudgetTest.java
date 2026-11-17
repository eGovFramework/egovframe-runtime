package org.egovframe.rte.ai.core.document;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link EgovExtractionBudget} 값 객체 테스트 — 기본값·무제한·유효성·한도 판정(R51-06, 스펙 ai/document-text-extraction "자원 예산").
 */
class EgovExtractionBudgetTest {

	@Test
	@DisplayName("기본 예산 값은 조회할 수 있고 zip 가드 기본값과 정합한다")
	void defaults() {
		EgovExtractionBudget budget = EgovExtractionBudget.defaults();
		assertEquals(256L * 1024 * 1024, budget.getMaxFileBytes());
		assertEquals(2_000, budget.getMaxPages());
		assertEquals(20_000_000L, budget.getMaxChars());
		assertEquals(EgovZipDocumentGuard.DEFAULT_MAX_ENTRIES, budget.getZipGuard().getMaxEntries());
		assertEquals(EgovZipDocumentGuard.DEFAULT_MAX_ENTRY_BYTES, budget.getZipGuard().getMaxEntryBytes());
		assertEquals(budget, EgovExtractionBudget.builder().build());
	}

	@Test
	@DisplayName("무제한은 명시적으로만 — 크기·페이지·문자·zip 크기 한도가 없다")
	void unlimited() {
		EgovExtractionBudget unlimited = EgovExtractionBudget.unlimited();
		assertEquals(Long.MAX_VALUE, unlimited.getMaxFileBytes());
		assertEquals(Integer.MAX_VALUE, unlimited.getMaxPages());
		assertEquals(Long.MAX_VALUE, unlimited.getMaxChars());
		assertEquals(Long.MAX_VALUE, unlimited.getZipGuard().getMaxTotalBytes());
		assertNotEquals(EgovExtractionBudget.defaults(), unlimited);
		assertDoesNotThrow(() -> unlimited.requireCharsWithinBudget(Long.MAX_VALUE, "x"));
		assertDoesNotThrow(() -> unlimited.requirePagesWithinBudget(Integer.MAX_VALUE, "x"));
	}

	@Test
	@DisplayName("한도는 양수여야 하고 가드는 null 일 수 없다")
	void validation() {
		assertThrows(IllegalArgumentException.class, () -> EgovExtractionBudget.builder().maxFileBytes(0));
		assertThrows(IllegalArgumentException.class, () -> EgovExtractionBudget.builder().maxPages(-1));
		assertThrows(IllegalArgumentException.class, () -> EgovExtractionBudget.builder().maxChars(0));
		assertThrows(NullPointerException.class, () -> EgovExtractionBudget.builder().zipGuard(null));
	}

	@Test
	@DisplayName("한도 판정은 어느 한도를 얼마나 넘었는지 담은 예산 초과 예외다")
	void limitChecks(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("two-kb.md");
		Files.writeString(file, "x".repeat(2048), StandardCharsets.UTF_8);
		EgovExtractionBudget budget = EgovExtractionBudget.builder().maxFileBytes(1024).maxPages(2).maxChars(100).build();

		EgovExtractionBudgetExceededException bytes = assertThrows(EgovExtractionBudgetExceededException.class,
				() -> budget.requireFileWithinBudget(file));
		assertEquals(EgovExtractionBudgetExceededException.Limit.FILE_BYTES, bytes.getLimit());
		assertEquals(1024, bytes.getAllowed());
		assertEquals(2048, bytes.getActual());
		assertEquals("two-kb.md", bytes.getFileName());
		assertTrue(bytes.getMessage().contains("파일 크기 한도"), bytes.getMessage());

		EgovExtractionBudgetExceededException pages = assertThrows(EgovExtractionBudgetExceededException.class,
				() -> budget.requirePagesWithinBudget(3, "a.pdf"));
		assertEquals(EgovExtractionBudgetExceededException.Limit.PAGES, pages.getLimit());

		EgovExtractionBudgetExceededException chars = assertThrows(EgovExtractionBudgetExceededException.class,
				() -> budget.requireCharsWithinBudget(101, "a.md"));
		assertEquals(EgovExtractionBudgetExceededException.Limit.CHARS, chars.getLimit());
		assertTrue(chars instanceof EgovExtractionException, "기존 추출 예외 처리 경로를 그대로 탄다");

		assertDoesNotThrow(() -> budget.requireCharsWithinBudget(100, "a.md"));
		assertDoesNotThrow(() -> budget.requirePagesWithinBudget(2, "a.pdf"));
		assertThrows(EgovExtractionException.class, () -> budget.requireFileWithinBudget(dir.resolve("missing.md")));
	}
}
