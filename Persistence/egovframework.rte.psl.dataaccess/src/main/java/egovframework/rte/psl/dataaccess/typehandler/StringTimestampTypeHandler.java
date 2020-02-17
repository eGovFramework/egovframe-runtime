/*
x * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * String - Timestamp 변환을 지원하는 TypeHandler 확장 클래스
 * <p>
 * <b>NOTE</b>: iBatis 의 TypeHandlerCallback 을
 * implements 하고 있으며 String 타입의 formatted 날짜시각 값을
 * java.sql.Timestamp 로 변환하여 DB 에 입력하고 조회할 수 있도록 처리하는
 * TypeHandler 이다.
 * </p>
 * @author 실행환경 개발팀 우병훈
 * @since 2009.02.21
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.02.21  우병훈          최초 생성
 * 
 * </pre>
 */
public class StringTimestampTypeHandler implements TypeHandlerCallback {

	/** date format - yyyyMMddHHmmss (14자리 년월일시분초) */
	private static final String DATE_FORMAT = "yyyyMMddHHmmss";
	/** SimpleDateFormat - DATE_FORMAT 기반 포맷터 */
	private static final SimpleDateFormat SDF = new SimpleDateFormat(DATE_FORMAT, java.util.Locale.getDefault());

	/**
	 * JDBC 의 Timestamp 로 조회된 값을 resultMap 처리 시 결과
	 * 객체(VO 또는 Map)의 String 타입 대상 Attribute 에
	 * yyyyMMddHHmmss formatted String 값으로 세팅한다.
	 * @param getter
	 *        - result set 으로 부터 현재 조회 필드를 얻을 수
	 *        있도록(칼럼명이나 index 없이도) 지원하는 ibatis 의
	 *        ResultGetter
	 * @return String 타입 결과 Attribute(yyyyMMddHHmmss 로
	 *         formatted 된 String)
	 * @exception SQLException
	 */
	public Object getResult(ResultGetter getter) throws SQLException {
		if (getter.wasNull()) {
			return null;
		}

		Timestamp ts = getter.getTimestamp();

		return SDF.format(ts);
	}

	/**
	 * Java 의 String 타입(yyyyMMddHHmmss 형식으로 format
	 * 맞춰진)으로 세팅된 입력 객체(VO 또는 Map)의 특정 Attribute 로 부터
	 * parameterMap(inline parameterMap) 처리 시 JDBC 의
	 * Timestamp 로 처리한다.
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
			try {
				Timestamp ts = new Timestamp(SDF.parse((String) parameter).getTime());
				setter.setTimestamp(ts);
			} catch (ParseException e) {
				throw new SQLException("Error parsing string to timestamp.  Cause: " + e.getMessage());
			}
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
