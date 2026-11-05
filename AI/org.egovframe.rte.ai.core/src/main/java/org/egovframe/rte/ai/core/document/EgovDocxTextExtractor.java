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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * DOCX(MS Word) 텍스트 추출기 — Apache POI(optional) 기반.
 *
 * <p>참고 샘플은 POI 를 얻으려고 {@code egovframe-rte-fdl-excel} 을 통째 의존했다(전이 의존 우회). 여기서는 {@code poi-ooxml}
 * 을 optional 로 직접 선언한다(과제 17 이후 전이 의존 금지 관례).</p>
 *
 * <p><b>zip 가드.</b> DOCX 는 zip 컨테이너이므로 POI 에 넘기기 전에 HWPX 와 <b>같은</b> {@link EgovZipDocumentGuard}(예산의 가드)를
 * 통과해야 한다 — 엔트리 수·개별·누적 해제 크기·DOCTYPE 을 POI 의 전역 설정에 기대지 않고 같은 기준으로 막는다(보안점검 1부 4장 #5).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core)
 *  2026.09.18  실행환경팀     예산의 zip 가드를 파싱 전에 적용 (R51-06 파싱 자원 예산 · 보안점검 #5)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovDocxTextExtractor implements EgovTextExtractor {

	/** 형식 이름. */
	public static final String TYPE = "docx";

	private static final String PROBE_CLASS = "org.apache.poi.xwpf.usermodel.XWPFDocument";
	private static final String COORDINATES = "org.apache.poi:poi-ooxml";

	private final EgovZipDocumentGuard guard;

	/** 기본 예산의 zip 가드를 쓴다. */
	public EgovDocxTextExtractor() {
		this(EgovExtractionBudget.defaults());
	}

	/**
	 * 지정한 예산의 zip 가드를 파싱 전에 적용한다.
	 *
	 * @since 5.1
	 */
	public EgovDocxTextExtractor(EgovExtractionBudget budget) {
		this.guard = Objects.requireNonNull(budget, "budget must not be null").getZipGuard();
	}

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public List<String> extensions() {
		return List.of("docx");
	}

	@Override
	public List<EgovExtractedDocument> extract(Path file) {
		Objects.requireNonNull(file, "file must not be null");
		EgovOptionalLibraries.require("DOCX 텍스트 추출", PROBE_CLASS, COORDINATES);
		String fileName = String.valueOf(file.getFileName());
		guard.check(file);
		String content;
		try {
			content = Poi.text(file);
		} catch (Exception e) {
			throw new EgovExtractionException("DOCX 파일을 읽지 못했습니다: " + fileName, e);
		}
		if (content == null || content.isBlank()) {
			return List.of();
		}
		String id = EgovDocumentIds.of(TYPE, fileName);
		Map<String, Object> metadata = EgovDocumentMetadata.base(TYPE, fileName, id, content.length());
		return List.of(new EgovExtractedDocument(id, content, metadata));
	}

	/** POI 타입 참조를 이 안에 가둔다. */
	static final class Poi {

		private Poi() {
		}

		static String text(Path file) throws Exception {
			try (InputStream in = Files.newInputStream(file);
					XWPFDocument document = new XWPFDocument(in);
					XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
				return extractor.getText();
			}
		}
	}
}
