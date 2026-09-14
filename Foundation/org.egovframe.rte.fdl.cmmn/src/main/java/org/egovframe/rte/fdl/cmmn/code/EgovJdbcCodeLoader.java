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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DB 에서 공통코드 전량을 읽는 {@link EgovCodeLoader} 구현.
 *
 * <p><b>NOTE:</b> 실행환경에는 표준 코드 테이블이 없으므로 <b>SQL 을 필수로 받는다</b> —
 * 테이블·컬럼명이 사업마다 다르기 때문이다. 대신 SELECT 결과의 <b>컬럼 별칭 계약</b>을 고정한다.</p>
 *
 * <table border="1">
 *   <caption>컬럼 별칭 계약</caption>
 *   <tr><th>별칭</th><th>필수</th><th>의미</th></tr>
 *   <tr><td>{@code GROUP_ID}</td><td>필수</td><td>코드그룹 ID</td></tr>
 *   <tr><td>{@code CODE}</td><td>필수</td><td>코드 값</td></tr>
 *   <tr><td>{@code NAME}</td><td>필수</td><td>표시명</td></tr>
 *   <tr><td>{@code DESCRIPTION}</td><td>선택</td><td>설명</td></tr>
 *   <tr><td>{@code ENABLED}</td><td>선택</td><td>사용 여부({@code Y/N}·{@code true/false}·{@code 1/0}) — 없으면 사용 중으로 본다</td></tr>
 *   <tr><td>{@code SORT_ORDER}</td><td>선택</td><td>정렬 순서 — 없으면 0</td></tr>
 * </table>
 *
 * <p>공통컴포넌트 표준 테이블이라면 이렇게 쓴다.</p>
 *
 * <pre>
 * new EgovJdbcCodeLoader(dataSource,
 *         "SELECT CODE_ID AS GROUP_ID, CODE, CODE_NM AS NAME, CODE_DC AS DESCRIPTION, "
 *       + "USE_AT AS ENABLED FROM COMTCCMMNDETAILCODE");
 * </pre>
 *
 * <p><b>사용 안 함 코드도 함께 읽는 것을 권장한다</b>(WHERE 로 거르지 않는다). 과거 데이터의
 * 코드 이름을 표시할 때는 사용 안 함 코드도 이름이 필요하기 때문이다 — 셀렉트박스처럼
 * 사용 중만 필요한 자리는 조회 쪽({@link EgovCodeCache#getActiveCodes(String)})에서 거른다.</p>
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
public class EgovJdbcCodeLoader implements EgovCodeLoader {

	private static final String COLUMN_GROUP_ID = "GROUP_ID";

	private static final String COLUMN_CODE = "CODE";

	private static final String COLUMN_NAME = "NAME";

	private static final String COLUMN_DESCRIPTION = "DESCRIPTION";

	private static final String COLUMN_ENABLED = "ENABLED";

	private static final String COLUMN_SORT_ORDER = "SORT_ORDER";

	private final JdbcTemplate jdbcTemplate;

	private final String sql;

	/**
	 * @param dataSource 코드 테이블이 있는 데이터소스(필수)
	 * @param sql        컬럼 별칭 계약을 지키는 SELECT 문(필수)
	 */
	public EgovJdbcCodeLoader(DataSource dataSource, String sql) {
		if (dataSource == null) {
			throw new IllegalArgumentException("dataSource must not be null");
		}
		if (sql == null || sql.trim().isEmpty()) {
			throw new IllegalArgumentException("sql must not be null or empty");
		}
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.sql = sql;
	}

	@Override
	public List<EgovCode> loadAll() {
		// DataAccessException(unchecked)은 그대로 전파한다 — 침묵 실패 금지.
		return jdbcTemplate.query(sql, EgovJdbcCodeLoader::extractAll);
	}

	private static List<EgovCode> extractAll(ResultSet rs) throws SQLException {
		// 컬럼 구성은 결과셋 전체에서 같으므로 메타데이터는 한 번만 읽는다.
		Set<String> columns = columnLabelsOf(rs.getMetaData());
		requireColumns(columns);

		List<EgovCode> codes = new ArrayList<>();
		while (rs.next()) {
			String description = columns.contains(COLUMN_DESCRIPTION)
					? rs.getString(COLUMN_DESCRIPTION) : "";
			boolean enabled = !columns.contains(COLUMN_ENABLED)
					|| isTruthy(rs.getString(COLUMN_ENABLED));
			int sortOrder = columns.contains(COLUMN_SORT_ORDER)
					? rs.getInt(COLUMN_SORT_ORDER) : 0;

			codes.add(new EgovCode(
					rs.getString(COLUMN_GROUP_ID),
					rs.getString(COLUMN_CODE),
					rs.getString(COLUMN_NAME),
					description, enabled, sortOrder));
		}
		return codes;
	}

	private static void requireColumns(Set<String> columns) {
		if (!columns.contains(COLUMN_GROUP_ID) || !columns.contains(COLUMN_CODE)
				|| !columns.contains(COLUMN_NAME)) {
			throw new IllegalStateException(
					"code SQL must select GROUP_ID, CODE, NAME (aliases) — found: " + columns);
		}
	}

	private static Set<String> columnLabelsOf(ResultSetMetaData metaData) throws SQLException {
		Set<String> labels = new HashSet<>();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			labels.add(metaData.getColumnLabel(i).toUpperCase(Locale.ROOT));
		}
		return labels;
	}

	/**
	 * 사용 여부 문자열을 판정한다({@code Y}·{@code true}·{@code 1} 이 사용 중).
	 *
	 * @param value DB 값({@code null} 이면 사용 안 함)
	 * @return 사용 중이면 {@code true}
	 */
	private static boolean isTruthy(String value) {
		if (value == null) {
			return false;
		}
		String normalized = value.trim().toUpperCase(Locale.ROOT);
		return "Y".equals(normalized) || "TRUE".equals(normalized) || "1".equals(normalized);
	}
}
