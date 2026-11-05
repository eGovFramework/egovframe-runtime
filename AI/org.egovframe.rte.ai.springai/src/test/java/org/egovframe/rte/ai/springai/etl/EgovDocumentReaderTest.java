package org.egovframe.rte.ai.springai.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.core.document.EgovExtractionBudget;
import org.egovframe.rte.ai.core.document.EgovExtractionBudgetExceededException;
import org.egovframe.rte.ai.core.document.EgovTextExtractors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.document.Document;

/**
 * 리더 계약 테스트 — 기반 추출기 위임·메타데이터 이관·엄격/부분 읽기(스펙 ai/springai-document-etl 1항).
 */
class EgovDocumentReaderTest {

	@TempDir
	Path dir;

	private Path markdown(String name, String body) throws IOException {
		Path file = dir.resolve(name);
		Files.writeString(file, body, StandardCharsets.UTF_8);
		return file;
	}

	@Test
	@DisplayName("정상 파일 2건은 Document 2건이 되고 기반 모듈 메타데이터를 그대로 갖는다")
	void readsMarkdownFiles() throws IOException {
		Path a = markdown("a.md", "# 안내\n전자정부 표준프레임워크는 공공 정보화 사업의 개발 기반이다.");
		Path b = markdown("b.md", "# 설정\n페이징은 PaginationInfo 로 설정한다.");

		List<Document> documents = EgovDocumentReader.of(List.of(a, b)).get();

		assertEquals(2, documents.size());
		Document first = documents.get(0);
		assertEquals("a.md", first.getMetadata().get(EgovDocumentMetadata.FILE_NAME));
		assertEquals("markdown", first.getMetadata().get(EgovDocumentMetadata.TYPE));
		assertNotNull(first.getMetadata().get(EgovDocumentMetadata.ORIGINAL_ID));
		assertEquals(first.getId(), first.getMetadata().get(EgovDocumentMetadata.ORIGINAL_ID), "원본 id = 문서 id");
		assertTrue(first.getText().contains("전자정부 표준프레임워크"));
	}

	@Test
	@DisplayName("엄격 읽기는 손상 파일이 섞이면 실패 파일명·원인을 담은 예외를 던진다")
	void strictReadFailsWithAllFailures() throws IOException {
		Path ok = markdown("ok.md", "정상 문서다.");
		Path broken = dir.resolve("broken.pdf");
		Files.write(broken, "this is not a pdf".getBytes(StandardCharsets.US_ASCII));

		EgovDocumentReadException e = assertThrows(EgovDocumentReadException.class,
				() -> EgovDocumentReader.of(List.of(ok, broken)).get());

		assertTrue(e.getMessage().contains("broken.pdf"), e.getMessage());
		assertNotNull(e.getCause(), "첫 실패 원인이 cause");
		assertEquals(1, e.failures().size());
		assertEquals(broken, e.failures().get(0).file());
	}

	@Test
	@DisplayName("부분 읽기는 성공 문서와 실패 목록을 함께 돌려준다")
	void lenientReadReturnsBoth() throws IOException {
		Path ok = markdown("ok.md", "정상 문서다.");
		Path broken = dir.resolve("broken.pdf");
		Files.write(broken, new byte[] {1, 2, 3});

		EgovDocumentReader.Result result = EgovDocumentReader.of(List.of(ok, broken)).readAll();

		assertEquals(1, result.documents().size());
		assertEquals(1, result.failures().size());
		assertTrue(result.hasFailures());
		assertEquals(broken, result.failures().get(0).file());
		assertFalse(result.failures().get(0).reason().isBlank());
		assertNotNull(result.failures().get(0).cause());
	}

	@Test
	@DisplayName("텍스트가 없는 파일은 빈 파일 목록으로 분리된다")
	void emptyFilesAreListed() throws IOException {
		Path empty = markdown("empty.md", "   \n");

		EgovDocumentReader.Result result = EgovDocumentReader.of(List.of(empty)).readAll();

		assertTrue(result.documents().isEmpty());
		assertTrue(result.failures().isEmpty());
		assertEquals(List.of(empty), result.empty());
	}

	@Test
	@DisplayName("예산 초과 파일은 실패 목록에 사유로 남는다 — 예산은 추출기 모음에서 오고 리더는 무수정 (R51-06)")
	void budgetExceededIsReportedAsFailure() throws IOException {
		Path ok = markdown("ok.md", "정상 문서다.");
		Path big = markdown("big.md", "x".repeat(4096));
		EgovTextExtractors budgeted = EgovTextExtractors.defaults(EgovExtractionBudget.builder().maxFileBytes(1024).build());

		EgovDocumentReader.Result result = EgovDocumentReader.of(budgeted, List.of(ok, big)).readAll();

		assertEquals(1, result.documents().size());
		assertEquals(1, result.failures().size());
		assertEquals(big, result.failures().get(0).file());
		assertTrue(result.failures().get(0).reason().contains("파일 크기 한도"), result.failures().get(0).reason());
		assertTrue(result.failures().get(0).cause() instanceof EgovExtractionBudgetExceededException);
	}
}
