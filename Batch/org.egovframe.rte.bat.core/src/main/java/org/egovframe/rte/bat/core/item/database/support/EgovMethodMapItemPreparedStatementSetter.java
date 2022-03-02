/*
 * Copyright 2012-2014 MOSPA(Ministry of Security and Public Administration).
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
package org.egovframe.rte.bat.core.item.database.support;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import org.egovframe.rte.bat.core.reflection.EgovReflectionSupport;
import org.springframework.util.ReflectionUtils;

/**
 * EgovMethodMapItemPreparedStatementSetter EgovItemPreparedStatementSetter를
 * 상속받아 param이 키값이고 Method가 Value인 Map을 통해 PreparedStatementSetter에 설정함.
 *
 * @author 배치실행개발팀
 * @since 2012.07.20
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.07.20	배치실행개발팀		최초 생성
 * 2012.07.30	배치실행개발팀		상속구조 수정
 * </pre>
 */
public class EgovMethodMapItemPreparedStatementSetter<T> extends EgovItemPreparedStatementSetter<T> {

	/**
	 * params 만큼 돌면서 sqlType별로 PreparedStatement에 자동셋팅시킴
	 */
	@Override
	public void setValues(T item, PreparedStatement ps, String[] params, String[] sqlTypes, Map<String, Method> methodMap) throws SQLException {
		EgovReflectionSupport<T> reflector = new EgovReflectionSupport<T>();

		for (int i = 0; i < params.length; i++) {
			try {
				if (sqlTypes[i].equals("String")) {
					ps.setString(i + 1, (String) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("int")) {
					ps.setInt(i + 1, (Integer) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("double")) {
					ps.setDouble(i + 1, (Double) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("Date")) {
					ps.setDate(i + 1, (Date) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("byte")) {
					ps.setByte(i + 1, (Byte) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("short")) {
					ps.setShort(i + 1, (Short) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("boolean")) {
					ps.setBoolean(i + 1, (Boolean) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("long")) {
					ps.setLong(i + 1, (Long) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("Float")) {
					ps.setFloat(i + 1, (Float) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("BigDecimal")) {
					ps.setBigDecimal(i + 1, (BigDecimal) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else if (sqlTypes[i].equals("byte[]")) {
					ps.setBytes(i + 1, (byte[]) reflector.invokeGettterMethod(item, params[i], methodMap));
				} else {
					throw new SQLException();
				}
			} catch (IllegalArgumentException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		}
	}

}
