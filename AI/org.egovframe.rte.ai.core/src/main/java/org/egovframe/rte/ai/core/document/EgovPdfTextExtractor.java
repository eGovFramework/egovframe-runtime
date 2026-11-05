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
package org.egovframe.rte.ai.core.document;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * PDF 텍스트 추출기 — Apache PDFBox 3.x(optional) 기반, <b>페이지 단위 문서</b>.
 *
 * <p>페이지마다 문서 하나를 만들고(식별자 <code>pdf-&lt;정제명&gt;_&lt;페이지&gt;</code>, 메타데이터 {@code page_number}) 공백뿐인
 * 페이지는 제외한다 — 빈 문서를 그대로 색인하면 무의미한 벡터가 검색 품질을 떨어뜨린다. pdfbox 는 Spring AI 1.0.1 과 같은
 * <b>3.0.x</b> 를 쓴다. LangChain4j 의 pdfbox 파서(2.0.x)와 같은 클래스패스에 두면 어느 한쪽이 깨지므로 혼용하지 않는다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core)
 *  2026.09.18  실행환경팀     자원 예산 — 페이지 수를 본문 읽기 전에 한도와 비교 (R51-06 파싱 자원 예산)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovPdfTextExtractor implements EgovTextExtractor {

	/** 형식 이름. */
	public static final String TYPE = "pdf";

	private static final String PROBE_CLASS = "org.apache.pdfbox.Loader";
	private static final String COORDINATES = "org.apache.pdfbox:pdfbox (3.0.x)";

	private final EgovExtractionBudget budget;

	/** 기본 예산(페이지 {@value EgovExtractionBudget#DEFAULT_MAX_PAGES})으로 만든다. */
	public EgovPdfTextExtractor() {
		this(EgovExtractionBudget.defaults());
	}

	/**
	 * 지정한 예산의 페이지 한도를 적용한다 — 문서를 연 뒤 페이지 수가 한도를 넘으면 본문을 읽지 않고 실패한다.
	 *
	 * @since 5.1
	 */
	public EgovPdfTextExtractor(EgovExtractionBudget budget) {
		this.budget = Objects.requireNonNull(budget, "budget must not be null");
	}

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public List<String> extensions() {
		return List.of("pdf");
	}

	@Override
	public List<EgovExtractedDocument> extract(Path file) {
		Objects.requireNonNull(file, "file must not be null");
		EgovOptionalLibraries.require("PDF 텍스트 추출", PROBE_CLASS, COORDINATES);
		String fileName = String.valueOf(file.getFileName());
		List<String> pages;
		try {
			pages = Pdfbox.pageTexts(file, budget, fileName);
		} catch (EgovExtractionBudgetExceededException e) {
			throw e;
		} catch (Exception e) {
			throw new EgovExtractionException("PDF 파일을 읽지 못했습니다: " + fileName, e);
		}
		List<EgovExtractedDocument> documents = new ArrayList<>();
		for (int i = 0; i < pages.size(); i++) {
			String content = pages.get(i);
			if (content == null || content.isBlank()) {
				continue;
			}
			int pageNumber = i + 1;
			String id = EgovDocumentIds.of(TYPE, fileName, pageNumber);
			Map<String, Object> metadata = EgovDocumentMetadata.base(TYPE, fileName, id, content.length());
			metadata.put(EgovDocumentMetadata.PAGE_NUMBER, pageNumber);
			documents.add(new EgovExtractedDocument(id, content, metadata));
		}
		return List.copyOf(documents);
	}

	/** pdfbox 타입 참조를 이 안에 가둔다. */
	static final class Pdfbox {

		private Pdfbox() {
		}

		static List<String> pageTexts(Path file, EgovExtractionBudget budget, String fileName) throws Exception {
			try (PDDocument document = Loader.loadPDF(file.toFile())) {
				int count = document.getNumberOfPages();
				budget.requirePagesWithinBudget(count, fileName);
				List<String> pages = new ArrayList<>(count);
				PDFTextStripper stripper = new PDFTextStripper();
				for (int page = 1; page <= count; page++) {
					stripper.setStartPage(page);
					stripper.setEndPage(page);
					pages.add(stripper.getText(document));
				}
				return pages;
			}
		}
	}
}
