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
import java.util.Locale;

/**
 * 문서 텍스트 추출기 SPI — 한 형식을 담당하며 파일에서 프레임워크 중립 문서 값을 만든다.
 *
 * <p>계약: 본문이 비어 있는 문서는 결과에 넣지 않는다(빈 목록). 파서 부재·손상·읽기 실패는 {@link EgovExtractionException}
 * 으로 알린다(빈 목록으로 삼키지 않는다). 식별자는 {@link EgovDocumentIds}, 메타데이터 키는 {@link EgovDocumentMetadata}
 * 규약을 따른다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public interface EgovTextExtractor {

	/** 형식 이름(hwp·hwpx·docx·pdf·markdown) — 식별자 접두·메타데이터 type. */
	String type();

	/** 담당 확장자(소문자, 점 없음). */
	List<String> extensions();

	/** 파일명으로 담당 여부를 판정한다(확장자, 대소문자 무관). */
	default boolean supports(String fileName) {
		if (fileName == null) {
			return false;
		}
		int dot = fileName.lastIndexOf('.');
		if (dot < 0 || dot == fileName.length() - 1) {
			return false;
		}
		String ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
		return extensions().contains(ext);
	}

	/**
	 * 추출한다.
	 *
	 * @param file 파일 경로
	 * @return 문서 목록(PDF 는 페이지마다 하나, 나머지는 하나 이하). 본문 없는 문서는 제외
	 * @throws EgovExtractionException 파서 부재·손상·읽기 실패
	 */
	List<EgovExtractedDocument> extract(Path file);
}
