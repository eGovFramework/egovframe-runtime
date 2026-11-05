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

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.ParagraphListInterface;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.reader.HWPReader;

/**
 * HWP(한글 5.0 이진) 텍스트 추출기 — hwplib(optional) 기반, <b>문단·표 구조 보존</b>.
 *
 * <p>hwplib 기본 추출기는 표를 순회하며 셀 텍스트만 이어 붙이고 행·셀 구분자를 넣지 않아 어느 값이 어느 행인지 복원할 수
 * 없다(참고 샘플 PR #77 실측). 문서 모델을 직접 순회해 문단은 개행, 표의 행은 개행, 셀은 <code> | </code> 로 잇는다. 구분자는
 * 셀 사이에만 넣고 행 맨 앞에는 붙이지 않는다(줄 맨 앞 파이프는 문장 분리기가 마크다운 표로 오인).</p>
 *
 * <p>hwplib 는 HWP 5.0 이진 규격만 지원한다 — 3.x 구형·배포용(DRM) 파일은 추출 예외가 된다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플 EgovHwpStructuredExtractor 설계 차용)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovHwpTextExtractor implements EgovTextExtractor {

	/** 형식 이름. */
	public static final String TYPE = "hwp";

	/** 표 셀 구분자(HWPX 추출과 동일). */
	public static final String CELL_SEPARATOR = " | ";

	private static final String PROBE_CLASS = "kr.dogfoot.hwplib.reader.HWPReader";
	private static final String COORDINATES = "kr.dogfoot:hwplib";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public List<String> extensions() {
		return List.of("hwp");
	}

	@Override
	public List<EgovExtractedDocument> extract(Path file) {
		Objects.requireNonNull(file, "file must not be null");
		EgovOptionalLibraries.require("HWP 텍스트 추출", PROBE_CLASS, COORDINATES);
		String fileName = String.valueOf(file.getFileName());
		String content;
		try {
			content = Hwplib.structuredText(file);
		} catch (Exception e) {
			throw new EgovExtractionException("HWP 파일을 읽지 못했습니다: " + fileName, e);
		}
		if (content == null || content.isBlank()) {
			return List.of();
		}
		String id = EgovDocumentIds.of(TYPE, fileName);
		Map<String, Object> metadata = EgovDocumentMetadata.base(TYPE, fileName, id, content.length());
		return List.of(new EgovExtractedDocument(id, content, metadata));
	}

	/** hwplib 타입 참조를 이 안에 가둔다 — 라이브러리 부재 시 바깥 클래스는 정상 로드된다. */
	static final class Hwplib {

		private Hwplib() {
		}

		static String structuredText(Path file) throws Exception {
			return structuredText(HWPReader.fromFile(file.toFile()));
		}

		/** 메모리 문서 모델에서 구조 보존 텍스트를 만든다(테스트·파이프라인 재사용). */
		static String structuredText(HWPFile hwpFile) throws Exception {
			StringBuilder text = new StringBuilder();
			for (Section section : hwpFile.getBodyText().getSectionList()) {
				appendParagraphs(text, section);
			}
			return text.toString();
		}

		private static void appendParagraphs(StringBuilder text, ParagraphListInterface paragraphs)
				throws UnsupportedEncodingException {
			for (int i = 0; i < paragraphs.getParagraphCount(); i++) {
				appendParagraph(text, paragraphs.getParagraph(i));
			}
		}

		private static void appendParagraph(StringBuilder text, Paragraph paragraph) throws UnsupportedEncodingException {
			if (paragraph == null) {
				return;
			}
			if (paragraph.getText() != null) {
				String paragraphText = clean(paragraph.getText().getNormalString(0));
				if (!paragraphText.isEmpty()) {
					appendLine(text, paragraphText);
				}
			}
			if (paragraph.getControlList() == null) {
				return;
			}
			for (Control control : paragraph.getControlList()) {
				if (control != null && control.getType() == ControlType.Table) {
					appendTable(text, (ControlTable) control);
				}
			}
		}

		private static void appendTable(StringBuilder text, ControlTable table) throws UnsupportedEncodingException {
			for (Row row : table.getRowList()) {
				StringBuilder line = new StringBuilder();
				for (Cell cell : row.getCellList()) {
					if (line.length() > 0) {
						line.append(CELL_SEPARATOR);
					}
					line.append(cellText(cell));
				}
				appendLine(text, line.toString());
			}
		}

		/** 셀 안의 문단을 공백으로 이어 한 줄로 만든다. */
		private static String cellText(Cell cell) throws UnsupportedEncodingException {
			StringBuilder cellText = new StringBuilder();
			ParagraphListInterface paragraphs = cell.getParagraphList();
			for (int i = 0; i < paragraphs.getParagraphCount(); i++) {
				Paragraph paragraph = paragraphs.getParagraph(i);
				if (paragraph == null || paragraph.getText() == null) {
					continue;
				}
				String paragraphText = clean(paragraph.getText().getNormalString(0));
				if (paragraphText.isEmpty()) {
					continue;
				}
				if (cellText.length() > 0) {
					cellText.append(' ');
				}
				cellText.append(paragraphText);
			}
			return cellText.toString();
		}

		/** 문단 끝 캐리지 리턴 등 제어 문자를 걷어낸다(우리가 넣는 개행과 겹쳐 빈 줄이 생기는 것을 막는다). */
		private static String clean(String raw) {
			if (raw == null) {
				return "";
			}
			StringBuilder cleaned = new StringBuilder(raw.length());
			for (int i = 0; i < raw.length(); i++) {
				char ch = raw.charAt(i);
				if (ch >= ' ' || ch == '\t') {
					cleaned.append(ch);
				}
			}
			return cleaned.toString().trim();
		}

		private static void appendLine(StringBuilder text, String line) {
			if (text.length() > 0) {
				text.append('\n');
			}
			text.append(line);
		}
	}
}
