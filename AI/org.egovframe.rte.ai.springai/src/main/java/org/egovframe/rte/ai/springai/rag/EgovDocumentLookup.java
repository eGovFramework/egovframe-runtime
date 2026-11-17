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
package org.egovframe.rte.ai.springai.rag;

import java.util.List;

import org.springframework.ai.document.Document;

/**
 * 문서 id 로 본문을 조회하는 SPI — 하이브리드 검색에서 어휘 채널에만 잡힌 문서를 결과에 포함하기 위해 쓴다.
 *
 * <p>{@code VectorStore} 에는 id 조회 API 가 없고 필터도 메타데이터 기준이라(문서 id 는 저장소마다 키/컬럼이 다르다) 포터블한
 * 기본 구현을 둘 수 없다. 앱이 저장소에 맞게 구현한다 — Redis 는 {@code JSON.GET <prefix><id>}, pgvector 는
 * {@code SELECT id, content, metadata FROM … WHERE id IN (…)}. 돌려주지 않은 id 는 결과에서 제외되고 경고가 남는다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 어댑터 규범 4: 어휘 단독 문서 포함 정책)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
@FunctionalInterface
public interface EgovDocumentLookup {

	/**
	 * id 목록의 문서를 돌려준다(없는 id 는 생략).
	 *
	 * @param ids 문서 id(비어 있지 않음)
	 * @return 찾은 문서(순서 무관)
	 */
	List<Document> findByIds(List<String> ids);
}
