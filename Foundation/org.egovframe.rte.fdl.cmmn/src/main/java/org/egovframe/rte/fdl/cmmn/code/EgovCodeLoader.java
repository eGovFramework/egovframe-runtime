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
package org.egovframe.rte.fdl.cmmn.code;

import java.util.List;

/**
 * 공통코드 전량을 읽어 오는 로더 SPI.
 *
 * <p><b>NOTE:</b> {@link EgovCodeCache} 는 이 인터페이스로만 원천을 본다 — 코드가 DB 에 있든
 * 파일·원격에 있든 로더 구현만 바꾸면 된다. 기본 구현으로 {@link EgovJdbcCodeLoader}(DB)와
 * {@link EgovInMemoryCodeLoader}(정적·테스트)를 제공한다.</p>
 *
 * <p><b>전량 적재 계약이다.</b> 공통코드는 전체가 메모리에 들어가는 크기(통상 수천 건)라
 * 그룹별 부분 적재보다 전량 스냅숏이 단순하고 정확하다. 그룹핑·정렬·불변화는 캐시가
 * 책임지므로 로더는 <b>평평한 목록만</b> 돌려주면 된다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
@FunctionalInterface
public interface EgovCodeLoader {

	/**
	 * 공통코드 전량을 읽는다.
	 *
	 * <p>실패는 <b>예외로 알린다</b>(부분 결과나 빈 목록으로 무마하지 않는다 — 침묵 실패 금지).
	 * 원천에 코드가 하나도 없는 것은 실패가 아니므로 빈 목록이 정당하다.</p>
	 *
	 * @return 코드 목록(순서 무관 — 정렬은 캐시가 한다)
	 */
	List<EgovCode> loadAll();
}
