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
package org.egovframe.rte.ai.core.retrieval;

import java.util.List;

/**
 * 어휘(lexical) 검색 SPI — 질의 문자열과 상한을 받아 문서 식별자의 순위(상위가 앞)를 돌려준다.
 *
 * <p>백엔드는 벡터 저장소에 종속된다(RediSearch·PostgreSQL pg_trgm) — AI 프레임워크에는 종속되지 않으므로 어느 어댑터와도
 * 조합할 수 있다. <b>백엔드 오류는 예외로 전파</b>한다: 빈 결과로 오류를 감추면 호출 측이 "매칭 없음"과 "장애"를 구분할 수
 * 없다. 밀집 단독으로 강등할지는 호출 측(어댑터)이 결정하고 기록한다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
@FunctionalInterface
public interface EgovLexicalSearch {

	/**
	 * 검색한다.
	 *
	 * @param query 사용자 질의(원문)
	 * @param topK 최대 회수 개수
	 * @return 매칭 문서 식별자 순위(상위가 앞). 매칭 없으면 빈 목록
	 * @throws RuntimeException 백엔드 오류(원인 포함)
	 */
	List<String> search(String query, int topK);
}
