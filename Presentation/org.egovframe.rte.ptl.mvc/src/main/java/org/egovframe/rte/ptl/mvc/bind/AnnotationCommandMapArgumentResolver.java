/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
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

import org.egovframe.rte.ptl.mvc.bind.annotation.CommandMap;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * AnnotationCommandMapArgumentResolver.java
 * <p/> 
 * <b>NOTE:</b> <pre> Spring3.1부터 AnnotationMethodHandlerAdapter은 deprecated되었으며 대신, RequestMappingHandlerAdapter를 사용해야한다.
 * RequestMappingHandlerAdapter에서 ArgumentResolver는 webArgumentResolver가 아닌 HandlerMethodArgumentResolver를 구현해야한다.
 * ( 클래스의 동작은 기존 CommandMapArgumentResolver와 동일 )
 *
 * Controller에서 화면(JSP) 입력값을 받기 위해서 일반적으로 Command(Form Class) 객체를 사용하지만, Map 객체를 사용하는걸 선호할 수 있다.
 * Sping MVC는 Controller의 argument를 분석하여 argument값을 customizing 할 수 있는 HandlerMethodArgumentResolver라는 interface를 제공한다.
 * AnnotationCommandMapArgumentResolver는 HandlerMethodArgumentResolver의 구현 클래스이다.
 * AnnotationCommandMapArgumentResolver는 Controller 메소드의 argument중에 @CommandMap으로 선언된  Map 객체가 있다면
 * HTTP request를 Map객체에 담는다.
 *                </pre>
 * @author 이영지
 * @since 2014.04.23
 * @version 3.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.04.23	이영지				최초 생성
 * 2014.12.11	이기하				@RequestParam으로 대체
 * </pre>
 * @deprecated This class has been deprecated.
 */
@Deprecated
public class AnnotationCommandMapArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Map.class.isAssignableFrom(parameter.getParameterType()) && parameter.hasParameterAnnotation(CommandMap.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
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

}
