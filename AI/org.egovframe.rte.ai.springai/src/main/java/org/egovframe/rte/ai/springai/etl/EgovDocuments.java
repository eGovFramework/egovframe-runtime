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
package org.egovframe.rte.ai.springai.etl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.springframework.ai.document.Document;

/**
 * 변환기 공용 — 불변 {@link Document} 를 같은 id·메타데이터로 본문만 바꿔 다시 만든다(내부용).
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
final class EgovDocuments {

	private EgovDocuments() {
	}

	/**
	 * 본문을 바꾼 새 문서 — id·score·콘텐츠 포매터 유지, 메타데이터 복사 후 {@code content_length}(있을 때만) 갱신과 추가 키 반영.
	 *
	 * @param source 원본
	 * @param text 새 본문
	 * @param extra 덧붙일 메타데이터(없으면 빈 맵)
	 * @return 새 문서
	 */
	static Document withText(Document source, String text, Map<String, Object> extra) {
		Map<String, Object> metadata = new LinkedHashMap<>(source.getMetadata());
		if (metadata.containsKey(EgovDocumentMetadata.CONTENT_LENGTH)) {
			metadata.put(EgovDocumentMetadata.CONTENT_LENGTH, text.length());
		}
		metadata.putAll(extra);
		Document.Builder builder = Document.builder().id(source.getId()).text(text).metadata(metadata);
		if (source.getScore() != null) {
			builder.score(source.getScore());
		}
		Document rebuilt = builder.build();
		rebuilt.setContentFormatter(source.getContentFormatter());
		return rebuilt;
	}
}
