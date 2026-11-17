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

/**
 * 어댑터가 청크·변환 문서에 덧붙이는 메타데이터 키 상수.
 *
 * <p>기반 모듈의 키({@link org.egovframe.rte.ai.core.document.EgovDocumentMetadata} — {@code original_id}·{@code file_name} 등)는
 * 그대로 재사용하고, 여기에는 Spring AI 파이프라인 단계(분할·마스킹)가 만들어 내는 키만 둔다. 키는 벡터 저장소의 필터
 * 필드로 쓰이므로 소문자 스네이크 표기(점·공백 없음)를 유지한다.</p>
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
public final class EgovSpringAiMetadata {

	/** 청크 순번(0부터) — 분할기가 붙인다. */
	public static final String CHUNK_INDEX = "chunk_index";

	/** 같은 원본에서 나온 청크 수 — 분할기가 붙인다. */
	public static final String CHUNK_COUNT = "chunk_count";

	/** 청크 수 한도에 걸려 본문 일부가 분할되지 않은 문서의 청크 표식(MARK 정책) — 값은 {@code true}. */
	public static final String TRUNCATED = "truncated";

	/** 색인용 PII 마스킹이 실제로 치환을 일으킨 문서의 표식 — 값은 {@code true}. */
	public static final String PII_MASKED = "pii_masked";

	private EgovSpringAiMetadata() {
	}
}
