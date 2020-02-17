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
package egovframework.rte.bat.core.item.database.support;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;
/**
 * EgovItemPreparedStatementSetter
 * EgovJdbcBatchItemWriter가 사용하는 클래스
 * 
 * @author 배치실행개발팀
 * @since 2012.07.20
 * @version 1.0
 * @param <T>
 * @see 
 * <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일              수정자                수정내용
 *  ---------   -----------   ---------------------------
 *  2012.07.20  배치실행개발팀     최초 생성
 *  2012.07.30  배치실행개발팀     상속구조 수정
 *  
 * </pre>
 */
public class EgovItemPreparedStatementSetter<T> implements ItemPreparedStatementSetter<T> {
	/**
	 * Spring에서 지원하는 setValues,
	 * PreparedStatement에 대한 매개 변수 값을 설정
	 */
	public void setValues(T item, PreparedStatement ps) throws SQLException {
		
	}
	
	/**
	 * Egov에서 지원하는 setValues,
	 * PreparedStatement에 대한 매개 변수 값을 설정
	 */
	public void setValues(T item, PreparedStatement ps, String[] params,
			String[] sqlTypes, Map<String, Method> methodMap)
			throws SQLException {

	}
	
}
