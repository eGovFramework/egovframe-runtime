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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 추출 문서 메타데이터 키 규약 — 리더(추출기)마다 같은 키를 쓰게 해 출처 표기·재색인 삭제 필터가 형식에 무관하게 동작한다.
 *
 * <p>공통 키 5: {@link #ORIGINAL_ID}·{@link #FILE_NAME}·{@link #SOURCE}·{@link #TYPE}·{@link #CONTENT_LENGTH}. PDF 는
 * {@link #PAGE_NUMBER}, Markdown 은 구조 키 5 를 더한다. 어댑터는 이 맵을 프레임워크 문서의 메타데이터로 그대로 옮긴다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovDocumentMetadata {

	/** 원본 문서 식별자 — 분할 뒤에도 원본을 식별해 재색인 시 이전 청크를 지우는 키. */
	public static final String ORIGINAL_ID = "original_id";
	/** 파일명. */
	public static final String FILE_NAME = "file_name";
	/** 출처(파일명과 동일 — 프레임워크 리더 관례와의 정합). */
	public static final String SOURCE = "source";
	/** 형식(hwp·hwpx·docx·pdf·markdown). */
	public static final String TYPE = "type";
	/** 본문 길이(문자 수). */
	public static final String CONTENT_LENGTH = "content_length";
	/** 페이지 번호(1-base, PDF). */
	public static final String PAGE_NUMBER = "page_number";
	/** 마크다운 — 헤더 유무. */
	public static final String HAS_HEADERS = "has_headers";
	/** 마크다운 — 코드 블록 유무. */
	public static final String HAS_CODE_BLOCKS = "has_code_blocks";
	/** 마크다운 — 링크 유무. */
	public static final String HAS_LINKS = "has_links";
	/** 마크다운 — 이미지 유무. */
	public static final String HAS_IMAGES = "has_images";
	/** 마크다운 — 줄 수. */
	public static final String LINE_COUNT = "line_count";

	private EgovDocumentMetadata() {
	}

	/**
	 * 공통 키 5 를 채운 메타데이터(수정 가능한 맵).
	 */
	public static Map<String, Object> base(String type, String fileName, String originalId, int contentLength) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put(ORIGINAL_ID, originalId);
		metadata.put(FILE_NAME, fileName);
		metadata.put(SOURCE, fileName);
		metadata.put(TYPE, type);
		metadata.put(CONTENT_LENGTH, contentLength);
		return metadata;
	}
}
