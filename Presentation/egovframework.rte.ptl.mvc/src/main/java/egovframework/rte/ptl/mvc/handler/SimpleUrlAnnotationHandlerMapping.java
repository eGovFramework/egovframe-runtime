/*
 * Copyright 2002-2008 MOPAS(Ministry of Public Administration and Security).
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
package egovframework.rte.ptl.mvc.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

/**
 * SimpleUrlAnnotationHandlerMapping.java
 * <p/><b>NOTE:</b> <pre> DefaultAnnotationHandlerMapping에 interceptor를 등록하면, 모든 annotation Controller에 interceptor가 적용되는 문제점이 있다.
 * 만일 annotation type의 Controller만을 사용해야 하는 상황이라면,아래의 SimpleUrlAnnotationHandlerMapping을 등록하고 사용하기 바란다.
 * url 단위로 Interceptor를 적용할 수 있는 적절한 대안이 Spring Source에서 나온다면
 * SimpleUrlAnnotationHandlerMapping는 deprecated 되어야 한다.
 * </pre>
 * <pre class="code">
 * &lt;bean class="......web.servlet.mvc.annotation.SimpleUrlAnnotationHandlerMapping" &gt;
 *   &lt;property name="interceptors"&gt;
 *     ...
 *   &lt;/property&gt;
 *   &lt;property name="urls"&gt;
 *   	&lt;set&gt;
 *   			&lt;value&gt;/lets.do&lt;/value&gt;
 *   			&lt;value&gt;/simple*.do&lt;/value&gt;
 *   			...
 *   	&lt;/set&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author 실행환경 개발팀 함철
 * @since 2009.06.01
 * @version 1.0
 * @see &lt;mvc:interceptors&gt; element
 * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30  함철            최초 생성
 *
 * </pre>
 * @deprecated This class has been deprecated.
 */
@Deprecated
public class SimpleUrlAnnotationHandlerMapping extends DefaultAnnotationHandlerMapping {

	private Set<String> urls;

	public void setUrls(Set<String> urls) {
		this.urls = urls;
	}

	/**
	 * <code>@RequestMapping</code>로 선언된 url중에 ApplicationContext에 정의된 url로 remapping해 return
	 * url mapping시에는 PathMatcher를 사용하는데, 별도로 등록한 PathMatcher가 없다면 AntPathMatcher를 사용한다.
	 * @param urlsArray - <code>@RequestMapping</code>로 선언된 url list
	 * @return urlsArray중에 설정된 url을 필터링해서 return.
	 */
	protected String[] remappingUrls(String[] urlsArray) {

		if (urlsArray == null) {
			return null;
		}

		ArrayList<String> remappedUrls = new ArrayList<String>();

		for (Iterator<String> it = this.urls.iterator(); it.hasNext();) {
			String urlPattern = (String) it.next();
			for (int i = 0; i < urlsArray.length; i++) {
				if (getPathMatcher().matchStart(urlPattern, urlsArray[i])) {
					remappedUrls.add(urlsArray[i]);
				}
			}
		}

		return (String[]) remappedUrls.toArray(new String[remappedUrls.size()]);

	}

	/**
	 * <code>@RequestMapping</code>로 선언된 url을 재정의하기 위해
	 * org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping의 
	 * 메소드 protected String[] determineUrlsForHandler(String beanName)를 override.
	 * 
	 * @param beanName - the name of the candidate bean
	 * @return 빈에 해당하는 URL list
	 */
	protected String[] determineUrlsForHandler(String beanName) {
		return remappingUrls(super.determineUrlsForHandler(beanName));
	}

}
