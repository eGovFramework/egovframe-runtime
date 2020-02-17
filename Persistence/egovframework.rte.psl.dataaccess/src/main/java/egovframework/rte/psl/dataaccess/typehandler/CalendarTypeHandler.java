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
package egovframework.rte.psl.dataaccess.typehandler;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

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
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.02.19  우병훈          최초 생성
 * 
 * </pre>
 */
public class CalendarTypeHandler implements TypeHandlerCallback {

	/**
	 * JDBC 의 Timestamp 로 조회된 값을 resultMap 처리 시 결과
	 * 객체(VO 또는 Map)의 Calendar 타입 대상 Attribute 에 세팅한다.
	 * @param getter
	 *        - result set 으로 부터 현재 조회 필드를 얻을 수
	 *        있도록(칼럼명이나 index 없이도) 지원하는 ibatis 의
	 *        ResultGetter
	 * @return Calendar 타입 결과 Attribute
	 * @exception SQLException
	 */
	public Object getResult(ResultGetter getter) throws SQLException {
		if (getter.wasNull()) {
			return null;
		}
		java.util.Calendar cal = java.util.Calendar.getInstance();
		java.sql.Timestamp ts = getter.getTimestamp(cal);

		cal.setTime(ts);

		return cal;
	}

	/**
	 * Java 의 Calendar 타입으로 세팅된 입력 객체(VO 또는 Map)의 특정
	 * Attribute 로 부터 parameterMap(inline parameterMap)
	 * 처리 시 JDBC 의 Timestamp 로 처리한다.
	 * @param setter
	 *        - prepared statement 의 현재 바인드 변수에 대한 값
	 *        세팅을 지원하는(index 없이) ibatis 의
	 *        ParameterSetter
	 * @param parameter
	 *        - 입력 객체
	 * @exception SQLException
	 */
	public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {

		if (parameter == null) {
			setter.setNull(java.sql.Types.DATE);
		} else {
			java.util.Calendar cal = (java.util.Calendar) parameter;
			setter.setTimestamp(new java.sql.Timestamp(cal.getTimeInMillis()), cal);
		}
	}

	/**
	 * 대상 필드의 String 표현값을 되돌려 줌. (현재 구현하지 않았음)
	 * @param s
	 *        - 대상 필드
	 * @return 대상 필드 String 표현값
	 */
	public Object valueOf(String s) {
		return s;
	}

}
