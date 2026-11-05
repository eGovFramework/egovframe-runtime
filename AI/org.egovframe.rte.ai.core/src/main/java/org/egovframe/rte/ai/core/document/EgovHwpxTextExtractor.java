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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.reader.HWPXReader;
import kr.dogfoot.hwpxlib.tool.textextractor.TextExtractMethod;
import kr.dogfoot.hwpxlib.tool.textextractor.TextExtractor;
import kr.dogfoot.hwpxlib.tool.textextractor.TextMarks;

/**
 * HWPX(한글 OWPML) 텍스트 추출기 — hwpxlib(optional) 기반, 문단·표 구조 보존.
 *
 * <p>hwpxlib 추출기는 표식({@code TextMarks})이 없으면 구분자를 하나도 넣지 않아 문서 전체가 한 줄이 된다. 문단·표 행·표 셀
 * 세 표식만 지정해 HWP 추출과 같은 형식(문단·행은 개행, 셀은 <code> | </code>)을 만든다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플 EgovHwpxReader 표식 설계 차용)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovHwpxTextExtractor implements EgovTextExtractor {

	/** 형식 이름. */
	public static final String TYPE = "hwpx";

	private static final String PROBE_CLASS = "kr.dogfoot.hwpxlib.reader.HWPXReader";
	private static final String COORDINATES = "kr.dogfoot:hwpxlib";

	private final EgovZipDocumentGuard guard;

	/** 기본 한도의 zip 문서 가드를 쓴다. */
	public EgovHwpxTextExtractor() {
		this(EgovZipDocumentGuard.defaults());
	}

	/** 지정한 zip 문서 가드를 쓴다(필수). */
	public EgovHwpxTextExtractor(EgovZipDocumentGuard guard) {
		this.guard = Objects.requireNonNull(guard, "guard must not be null");
	}

	/**
	 * 지정한 예산의 zip 가드를 쓴다 — {@link EgovTextExtractors#defaults(EgovExtractionBudget)} 가 쓰는 생성자(R51-06, 2026-09-18).
	 *
	 * @since 5.1
	 */
	public EgovHwpxTextExtractor(EgovExtractionBudget budget) {
		this(Objects.requireNonNull(budget, "budget must not be null").getZipGuard());
	}

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public List<String> extensions() {
		return List.of("hwpx");
	}

	@Override
	public List<EgovExtractedDocument> extract(Path file) {
		Objects.requireNonNull(file, "file must not be null");
		EgovOptionalLibraries.require("HWPX 텍스트 추출", PROBE_CLASS, COORDINATES);
		String fileName = String.valueOf(file.getFileName());
		guard.check(file);
		String content;
		try {
			content = Hwpxlib.structuredText(file);
		} catch (Exception e) {
			throw new EgovExtractionException("HWPX 파일을 읽지 못했습니다: " + fileName, e);
		}
		if (content == null || content.isBlank()) {
			return List.of();
		}
		String id = EgovDocumentIds.of(TYPE, fileName);
		Map<String, Object> metadata = EgovDocumentMetadata.base(TYPE, fileName, id, content.length());
		return List.of(new EgovExtractedDocument(id, content, metadata));
	}

	/** hwpxlib 타입 참조를 이 안에 가둔다. */
	static final class Hwpxlib {

		/** 문단·표 행·표 셀 표식 — 값을 바꾸지 않으므로 공유한다. */
		private static final TextMarks STRUCTURE_MARKS = new TextMarks()
				.paraSeparatorAnd("\n")
				.tableRowSeparatorAnd("\n")
				.tableCellSeparatorAnd(EgovHwpTextExtractor.CELL_SEPARATOR);

		private Hwpxlib() {
		}

		static String structuredText(Path file) throws Exception {
			return structuredText(HWPXReader.fromFile(file.toFile()));
		}

		/** 메모리 문서 모델에서 구조 보존 텍스트를 만든다(테스트·파이프라인 재사용). */
		static String structuredText(HWPXFile hwpxFile) throws Exception {
			return TextExtractor.extract(hwpxFile, TextExtractMethod.InsertControlTextBetweenParagraphText, false,
					STRUCTURE_MARKS);
		}
	}
}
