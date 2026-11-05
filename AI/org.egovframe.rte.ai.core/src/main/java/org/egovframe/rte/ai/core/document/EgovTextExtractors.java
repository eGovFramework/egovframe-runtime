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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 추출기 레지스트리 — 파일명으로 담당 추출기를 고르고 여러 파일을 일괄 추출한다.
 *
 * <pre>
 * EgovTextExtractors extractors = EgovTextExtractors.defaults();          // hwp·hwpx·docx·pdf·markdown, 기본 예산
 * EgovExtractionResult result = extractors.extractAll(paths);
 * if (result.hasFailures()) { ... 실패 파일·사유를 보고 ... }
 *
 * // 자원 예산 조정 — 초대형 문서를 다루는 자리
 * EgovTextExtractors big = EgovTextExtractors.defaults(EgovExtractionBudget.builder().maxPages(10_000).build());
 * </pre>
 *
 * <p><b>자원 예산.</b> 모음은 {@link EgovExtractionBudget} 을 가지며, 어떤 추출기(사용자 구현 포함)든 <b>파일을 열기 전에</b> 파일 바이트
 * 한도를, <b>추출 뒤에</b> 한 파일의 결과 문자 수 한도를 적용한다. 내장 추출기는 형식 안쪽 한도(PDF 페이지·zip 컨테이너)를 같은 예산에서
 * 받는다. 예산 초과는 {@link EgovExtractionBudgetExceededException} 이며 부분 결과를 돌려주지 않는다. 기본 예산이 있고 무제한은
 * {@link EgovExtractionBudget#unlimited()} 로 명시한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core)
 *  2026.09.18  실행환경팀     자원 예산(EgovExtractionBudget) 적용 — defaults(budget)·of(list, budget)·파일 크기 선검사·문자 수 후검사
 *                            (R51-06 파싱 자원 예산)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovTextExtractors {

	private final List<EgovTextExtractor> extractors;
	private final EgovExtractionBudget budget;

	private EgovTextExtractors(List<EgovTextExtractor> extractors, EgovExtractionBudget budget) {
		this.extractors = List.copyOf(extractors);
		this.budget = budget;
	}

	/** 기본 5종(HWP·HWPX·DOCX·PDF·Markdown) + 기본 예산. 파서 라이브러리는 실제 추출 시점에 확인한다. */
	public static EgovTextExtractors defaults() {
		return defaults(EgovExtractionBudget.defaults());
	}

	/**
	 * 기본 5종에 지정한 예산을 적용해 만든다 — 내장 추출기의 형식 안쪽 한도(PDF 페이지·zip 가드)도 이 예산에서 온다.
	 *
	 * @since 5.1
	 */
	public static EgovTextExtractors defaults(EgovExtractionBudget budget) {
		Objects.requireNonNull(budget, "budget must not be null");
		return of(List.of(new EgovHwpTextExtractor(), new EgovHwpxTextExtractor(budget), new EgovDocxTextExtractor(budget),
				new EgovPdfTextExtractor(budget), new EgovMarkdownTextExtractor()), budget);
	}

	/** 지정한 추출기 목록으로 만든다(순서 = 담당 판정 우선순위) — 기본 예산 적용. */
	public static EgovTextExtractors of(List<EgovTextExtractor> extractors) {
		return of(extractors, EgovExtractionBudget.defaults());
	}

	/**
	 * 지정한 추출기 목록과 예산으로 만든다. 예산의 파일 크기·문자 수 한도는 사용자 구현 추출기에도 적용된다(형식 안쪽 한도는
	 * 각 추출기 생성자로 준다).
	 *
	 * @since 5.1
	 */
	public static EgovTextExtractors of(List<EgovTextExtractor> extractors, EgovExtractionBudget budget) {
		Objects.requireNonNull(extractors, "extractors must not be null");
		Objects.requireNonNull(budget, "budget must not be null");
		if (extractors.isEmpty()) {
			throw new IllegalArgumentException("추출기가 하나 이상 필요합니다");
		}
		return new EgovTextExtractors(extractors, budget);
	}

	/** 등록된 추출기. */
	public List<EgovTextExtractor> extractors() {
		return extractors;
	}

	/** 적용 중인 자원 예산. */
	public EgovExtractionBudget budget() {
		return budget;
	}

	/** 파일명을 담당하는 추출기. */
	public Optional<EgovTextExtractor> forFile(String fileName) {
		for (EgovTextExtractor extractor : extractors) {
			if (extractor.supports(fileName)) {
				return Optional.of(extractor);
			}
		}
		return Optional.empty();
	}

	/**
	 * 파일 하나를 추출한다 — 파일 크기 한도는 열기 전에, 문자 수 한도는 결과 전체에 적용한다.
	 *
	 * @throws EgovExtractionException               담당 추출기 없음·추출 실패
	 * @throws EgovExtractionBudgetExceededException 예산 초과(부분 결과 없음)
	 */
	public List<EgovExtractedDocument> extract(Path file) {
		Objects.requireNonNull(file, "file must not be null");
		String name = String.valueOf(file.getFileName());
		EgovTextExtractor extractor = forFile(name).orElseThrow(
				() -> new EgovExtractionException("지원하지 않는 문서 형식입니다: " + name));
		budget.requireFileWithinBudget(file);
		List<EgovExtractedDocument> documents = extractor.extract(file);
		long chars = 0L;
		for (EgovExtractedDocument document : documents) {
			chars += document.text() == null ? 0 : document.text().length();
		}
		budget.requireCharsWithinBudget(chars, name);
		return documents;
	}

	/**
	 * 여러 파일을 일괄 추출한다 — 실패한 파일은 사유와 함께 결과에 남기고 나머지는 계속 처리한다.
	 */
	public EgovExtractionResult extractAll(Collection<Path> files) {
		Objects.requireNonNull(files, "files must not be null");
		List<EgovExtractedDocument> succeeded = new ArrayList<>();
		List<EgovExtractionResult.Failure> failed = new ArrayList<>();
		List<Path> empty = new ArrayList<>();
		for (Path file : files) {
			try {
				List<EgovExtractedDocument> documents = extract(file);
				if (documents.isEmpty()) {
					empty.add(file);
				} else {
					succeeded.addAll(documents);
				}
			} catch (RuntimeException e) {
				failed.add(new EgovExtractionResult.Failure(file, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(), e));
			}
		}
		return new EgovExtractionResult(succeeded, failed, empty);
	}
}
