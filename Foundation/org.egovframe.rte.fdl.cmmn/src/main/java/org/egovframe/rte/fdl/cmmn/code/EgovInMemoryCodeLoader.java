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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 고정된 코드 목록을 그대로 돌려주는 {@link EgovCodeLoader} 구현.
 *
 * <p><b>NOTE:</b> DB 없이 코드를 쓰는 두 자리를 위한 것이다 — <b>테스트</b>, 그리고
 * 배포 후 바뀔 일이 없는 <b>정적 코드</b>(예: 요일·성별처럼 소스에 두는 편이 맞는 것).
 * 운영 중 관리되는 코드라면 {@link EgovJdbcCodeLoader} 를 쓴다.</p>
 *
 * <pre>
 * EgovCodeCache cache = new EgovCodeCache(new EgovInMemoryCodeLoader(List.of(
 *         EgovCode.of("GENDER", "M", "남성"),
 *         EgovCode.of("GENDER", "F", "여성"))));
 * </pre>
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
public class EgovInMemoryCodeLoader implements EgovCodeLoader {

	private final List<EgovCode> codes;

	/**
	 * @param codes 제공할 코드 목록(복사해 보관하며 {@code null} 항목은 허용하지 않는다)
	 */
	public EgovInMemoryCodeLoader(Collection<EgovCode> codes) {
		if (codes == null) {
			throw new IllegalArgumentException("codes must not be null");
		}
		List<EgovCode> copied = new ArrayList<>(codes.size());
		for (EgovCode code : codes) {
			if (code == null) {
				throw new IllegalArgumentException("codes must not contain null");
			}
			copied.add(code);
		}
		this.codes = Collections.unmodifiableList(copied);
	}

	@Override
	public List<EgovCode> loadAll() {
		return codes;
	}
}
