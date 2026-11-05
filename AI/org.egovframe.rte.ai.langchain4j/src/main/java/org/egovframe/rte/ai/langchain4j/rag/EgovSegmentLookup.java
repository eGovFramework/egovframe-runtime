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
package org.egovframe.rte.ai.langchain4j.rag;

import java.util.List;
import java.util.Map;

import dev.langchain4j.data.segment.TextSegment;

/**
 * 세그먼트 식별자로 본문을 조회하는 SPI — 하이브리드 검색에서 어휘 채널에만 잡힌 세그먼트를 결과에 포함하기 위해 쓴다.
 *
 * <p>LangChain4j {@code EmbeddingStore} 에는 id 조회 API 가 없어 앱이 저장소에 맞게 구현한다 — pgvector 는 {@link EgovJdbcSegmentLookup},
 * 그 밖의 저장소는 그 클라이언트로. {@link TextSegment} 에는 id 가 없으므로 식별자 → 세그먼트 맵으로 돌려준다. 돌려주지 않은 식별자는
 * 결과에서 제외되고 경고가 남는다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 어댑터 규범 4: 어휘 단독 문서 포함 정책)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
@FunctionalInterface
public interface EgovSegmentLookup {

	/**
	 * 식별자 목록의 세그먼트를 돌려준다(없는 식별자는 생략).
	 *
	 * @param ids 세그먼트 식별자(비어 있지 않음)
	 * @return 식별자 → 세그먼트
	 */
	Map<String, TextSegment> findByIds(List<String> ids);
}
