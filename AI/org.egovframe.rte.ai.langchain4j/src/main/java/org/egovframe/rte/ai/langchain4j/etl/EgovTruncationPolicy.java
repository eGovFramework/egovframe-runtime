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
package org.egovframe.rte.ai.langchain4j.etl;

/**
 * 분할기가 청크 수 한도에 걸려 본문 일부를 분할하지 못했을 때의 정책 — 기본 FAIL(예외), MARK 는 그 문서의 모든 세그먼트에
 * {@code truncated="true"} 표식을 붙이고 경고를 남긴 뒤 계속한다(검토 4장 #12 교정 — 초과분 폐기 후 완료 기록 금지).
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public enum EgovTruncationPolicy {

	/** {@link EgovChunkLimitException} 을 던진다(기본). */
	FAIL,

	/** 모든 세그먼트에 {@link EgovLangChain4jMetadata#TRUNCATED} 표식을 붙이고 경고를 남긴 뒤 계속한다. */
	MARK
}
