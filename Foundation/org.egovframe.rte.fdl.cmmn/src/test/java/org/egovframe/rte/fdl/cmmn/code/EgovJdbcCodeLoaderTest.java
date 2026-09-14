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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link EgovJdbcCodeLoader} 단위 테스트(HSQLDB 인메모리).
 *
 * <p>컬럼 별칭 계약 — 필수 3(GROUP_ID·CODE·NAME)·선택 3(DESCRIPTION·ENABLED·SORT_ORDER)과
 * 사용 여부 문자열 판정, 그리고 {@link EgovCodeCache} 와의 통합을 확인한다.</p>
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
class EgovJdbcCodeLoaderTest {

	private static JDBCDataSource dataSource;

	@BeforeAll
	static void setUpDatabase() {
		dataSource = new JDBCDataSource();
		dataSource.setUrl("jdbc:hsqldb:mem:egovCodeLoaderTest");
		dataSource.setUser("sa");

		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		jdbc.execute("DROP TABLE COMMON_CODE IF EXISTS");
		jdbc.execute("CREATE TABLE COMMON_CODE ("
				+ "CODE_ID VARCHAR(20) NOT NULL, CODE VARCHAR(20) NOT NULL, "
				+ "CODE_NM VARCHAR(100) NOT NULL, CODE_DC VARCHAR(200), "
				+ "USE_AT VARCHAR(5), CODE_ORD INT, "
				+ "PRIMARY KEY (CODE_ID, CODE))");
		jdbc.update("INSERT INTO COMMON_CODE VALUES ('COM001', 'A01', '정상', '정상 상태', 'Y', 1)");
		jdbc.update("INSERT INTO COMMON_CODE VALUES ('COM001', 'A02', '중지', NULL, 'N', 2)");
		jdbc.update("INSERT INTO COMMON_CODE VALUES ('COM001', 'A03', '대기', '', 'true', 3)");
		jdbc.update("INSERT INTO COMMON_CODE VALUES ('GENDER', 'M', '남성', NULL, '1', 0)");
	}

	private static final String FULL_SQL =
			"SELECT CODE_ID AS GROUP_ID, CODE, CODE_NM AS NAME, CODE_DC AS DESCRIPTION, "
					+ "USE_AT AS ENABLED, CODE_ORD AS SORT_ORDER FROM COMMON_CODE";

	@Test
	@DisplayName("6컬럼 전부 — 값이 그대로 매핑된다")
	void 전체_컬럼_매핑() {
		List<EgovCode> codes = new EgovJdbcCodeLoader(dataSource, FULL_SQL).loadAll();

		assertEquals(4, codes.size());
		EgovCode a01 = codes.stream()
				.filter(c -> "COM001".equals(c.getGroupId()) && "A01".equals(c.getCode()))
				.findFirst().orElseThrow();
		assertEquals("정상", a01.getName());
		assertEquals("정상 상태", a01.getDescription());
		assertTrue(a01.isEnabled());
		assertEquals(1, a01.getSortOrder());
	}

	@Test
	@DisplayName("사용 여부 판정 — Y·true·1 은 사용 중, N 은 사용 안 함")
	void 사용여부_판정() {
		List<EgovCode> codes = new EgovJdbcCodeLoader(dataSource, FULL_SQL).loadAll();

		assertTrue(find(codes, "A01").isEnabled(), "Y");
		assertTrue(!find(codes, "A02").isEnabled(), "N");
		assertTrue(find(codes, "A03").isEnabled(), "true");
		assertTrue(find(codes, "M").isEnabled(), "1");
	}

	@Test
	@DisplayName("필수 3컬럼만으로도 동작한다 — 사용 중·정렬 0 이 기본")
	void 필수_컬럼만() {
		List<EgovCode> codes = new EgovJdbcCodeLoader(dataSource,
				"SELECT CODE_ID AS GROUP_ID, CODE, CODE_NM AS NAME FROM COMMON_CODE").loadAll();

		assertEquals(4, codes.size());
		EgovCode a02 = find(codes, "A02");
		assertTrue(a02.isEnabled(), "ENABLED 컬럼이 없으면 사용 중으로 본다");
		assertEquals(0, a02.getSortOrder());
		assertEquals("", a02.getDescription());
	}

	@Test
	@DisplayName("필수 별칭이 빠지면 즉시 실패한다 — 잘못된 SQL 이 빈 결과로 무마되지 않는다")
	void 필수_별칭_누락() {
		EgovJdbcCodeLoader loader = new EgovJdbcCodeLoader(dataSource,
				"SELECT CODE_ID, CODE, CODE_NM FROM COMMON_CODE");   // 별칭 없음

		assertThrows(RuntimeException.class, loader::loadAll);
	}

	@Test
	@DisplayName("NULL 설명은 빈 문자열이 된다")
	void null_설명() {
		List<EgovCode> codes = new EgovJdbcCodeLoader(dataSource, FULL_SQL).loadAll();

		assertEquals("", find(codes, "A02").getDescription());
	}

	@Test
	@DisplayName("생성 인자 검증 — dataSource·sql 필수")
	void 생성_인자() {
		assertThrows(IllegalArgumentException.class, () -> new EgovJdbcCodeLoader(null, FULL_SQL));
		assertThrows(IllegalArgumentException.class, () -> new EgovJdbcCodeLoader(dataSource, " "));
	}

	@Test
	@DisplayName("캐시와 통합 — DB 코드가 스냅숏으로 조회된다")
	void 캐시_통합() {
		EgovCodeCache cache = new EgovCodeCache(new EgovJdbcCodeLoader(dataSource, FULL_SQL));

		assertEquals(Optional.of("정상"), cache.getName("COM001", "A01"));
		assertEquals(List.of("A01", "A03"),
				cache.getActiveCodes("COM001").stream().map(EgovCode::getCode).toList());
		assertEquals(3, cache.getCodes("COM001").size(), "사용 안 함(A02) 포함");
	}

	private static EgovCode find(List<EgovCode> codes, String code) {
		return codes.stream().filter(c -> code.equals(c.getCode())).findFirst().orElseThrow();
	}
}
