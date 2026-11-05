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
 * 분할기가 청크 수 한도에 걸려 본문 일부를 분할하지 못했을 때의 정책.
 *
 * <p>샘플은 초과분을 버리고 경고만 남긴 채 색인을 "완료" 로 기록했다(검토 4장 #12). 여기서는 기본이 실패(FAIL)이고, 계속하려면
 * 표식(MARK)을 명시해야 한다 — 그러면 그 문서의 모든 청크에 {@code truncated=true} 가 붙어 저장소에서 잘린 문서를 찾을 수 있다.</p>
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
public enum EgovTruncationPolicy {

	/** 문서 id 를 담은 {@link EgovChunkLimitException} 을 던진다(기본). */
	FAIL,

	/** 그 문서의 모든 청크에 {@link EgovSpringAiMetadata#TRUNCATED} 표식을 붙이고 경고를 남긴 뒤 계속한다. */
	MARK
}
