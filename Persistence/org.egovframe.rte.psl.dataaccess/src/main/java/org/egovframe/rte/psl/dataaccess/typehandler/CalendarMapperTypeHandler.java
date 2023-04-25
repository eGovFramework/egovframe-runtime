/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.rte.psl.dataaccess.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Calendar Type - Timestamp 변환을 지원하는 TypeHandler 확장 클래스
 * <p>
 * <b>NOTE</b>: iBatis 의 TypeHandlerCallback 을
 * implements 하고 있으며 java.util.Calendar 타입을
 * java.sql.Timestamp 로 변환하여 DB 에 입력하고 조회할 수 있도록 처리하는
 * TypeHandler 이다.
 * </p>
 * @author 실행환경 개발팀 우병훈
 * @since 2009.02.19
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.02.19	우병훈				최초 생성
 * </pre>
 */
public class CalendarMapperTypeHandler implements TypeHandler<Calendar> {

	public void setParameter(PreparedStatement ps, int i, Calendar parameter, JdbcType jdbcType) throws SQLException {
		if (parameter == null) {
			ps.setNull(i, java.sql.Types.DATE);
		} else {
			ps.setTimestamp(i, new java.sql.Timestamp(parameter.getTimeInMillis()));
		}
	}

	public Calendar getResult(ResultSet rs, String columnName) throws SQLException {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		if (rs.getTimestamp(columnName) == null) {
			return null;
		} else {
			java.sql.Timestamp ts = rs.getTimestamp(columnName);
			cal.setTime(ts);
			return cal;
		}
	}

	public Calendar getResult(ResultSet rs, int columnIndex) throws SQLException {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		java.sql.Timestamp ts = rs.getTimestamp(columnIndex);
		cal.setTime(ts);
		return cal;
	}

	public Calendar getResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}

}
