/*
 * Copyright 2008-2014 MOSPA(Ministry of Security and Public Administration).
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

package org.egovframe.rte.ptl.mvc.bind;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * CommandMapArgumentResolver.java
 * <p/><b>NOTE:</b> <pre> Controller에서 화면(JSP) 입력값을 받기 위해서 일반적으로 Command(Form Class) 객체를 사용하지만, Map 객체를 사용하는걸 선호할 수 있다.
 * Sping MVC는 Controller의 argument를 분석하여 argument값을 customizing 할 수 있는 WebArgumentResolver라는 interface를 제공한다.
 * CommandMapArgumentResolver는 WebArgumentResolver의 구현 클래스이다.
 * CommandMapArgumentResolver는 Controller 메소드의 argument중에 commandMap이라는 Map 객체가 있다면
 * HTTP request 객체에 있는 파라미터이름과 값을 commandMap에 담는다.
 * </pre>
 * @author 실행환경 개발팀 함철
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	함철				최초 생성
 * 2014.04.23	이영지				deprecated
 * </pre>
 * @deprecated This class has been deprecated.
 */
@Deprecated
public class CommandMapArgumentResolver implements WebArgumentResolver {

	/**
	 * Controller의 메소드 argument에 commandMap이라는 Map 객체가 있다면 
	 * HTTP request 객체에 있는 파라미터이름과 값을 commandMap에 담아 returng한다.
	 * 배열인 파라미터 값은 배열로 Map에 저장한다.
	 * 
	 * @param methodParameter - 메소드 파라미터의 타입,인덱스등의 정보 
	 * @param webRequest - web request 객체
	 * @return argument에 commandMap(java.util.Map)이 있으면 commandMap, 없으면<code>UNRESOLVED</code>.
	 * @exception Exception
	 */
	public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
		Class<?> clazz = methodParameter.getParameterType();
		String paramName = methodParameter.getParameterName();
		if (clazz.equals(Map.class) && paramName.equals("commandMap")) {
			Map<String, Object> commandMap = new HashMap<String, Object>();
			HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
			Enumeration<?> enumeration = request.getParameterNames();
			while (enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				String[] values = request.getParameterValues(key);
				if (values != null) {
					commandMap.put(key, (values.length > 1) ? values : values[0]);
				}
			}
			return commandMap;
		}
		return UNRESOLVED;
	}

}
