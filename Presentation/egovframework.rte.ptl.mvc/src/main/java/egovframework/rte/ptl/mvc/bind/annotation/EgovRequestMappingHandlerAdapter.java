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
package egovframework.rte.ptl.mvc.bind.annotation;

import java.util.ArrayList;
import java.util.List;

import egovframework.rte.ptl.mvc.bind.AnnotationCommandMapArgumentResolver;

import org.springframework.web.method.annotation.MapMethodProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * EgovRequestMappingHandlerAdapter.java
 * <p/><b>NOTE:</b> <pre>Spring 3.1부터 AnnotationMethodHandlerAdapter는 deprecated되고 RequestMappingHandlerAdapter를 대신 써야한다.
 * 또한 Map타입의 Argument를 처리해주는 MapMethodProcessor가 추가되어 RequestMappingHandlerAdapter를 그대로 써줄 경우
 * Egov에서 제공하는 CommandMapArgumentResolver를 쓸 수 없다.
 * HandlerMethodArgumentResolver을 implements하는  AnnotationCommandMapArgumentResolver를 구현하였으며
 * EgovRequestMapping에서는 MapMethodProcessor보다 AnnotationCommandMapArgumentResolver가 먼저 ArgumentResolver로  인식될 수 있도록
 * ArgumentResolver list의 순서를 변경해준다.
 *
 * </pre>
 * @author 이영지
 * @since 2014.04.23
 * @version 3.0
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.04.23  이영지            최초 생성
 *   2014.12.11  이기하		@RequestParam으로 대체
 *   2015.04.10  장동한		resolvers 수정 (나중에 삭제 처리해야 됨)
 *
 * </pre>
 * @deprecated This class has been deprecated.
 */
@Deprecated
public class EgovRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

	/**
	 * CommandMapMethodArgumentResolver를 쓰기 위하여
	 * ArgumentResolver list에서 MapMethodProcessor보다 앞에 CommandMapMethodArgumentResolver를 추가한다.
	 * MapMethodProcessor의 기능을 살리기 위해 CommandMapArgument를 쓸 때는 @CommandMap을 붙여야 한다.
	 *
	 * @see CommandMap
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		if (getArgumentResolvers() != null) {
			//List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>(getArgumentResolvers().getResolvers());
			List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>(getArgumentResolvers());

			int mapMethodProcessorInx = -1;
			int commandMapInx = -1;
			HandlerMethodArgumentResolver commandMapArgResolver = null;

			for (int inx = 0; inx < resolvers.size(); inx++) {
				HandlerMethodArgumentResolver resolver = resolvers.get(inx);
				if (resolver instanceof MapMethodProcessor) {
					mapMethodProcessorInx = inx;
				} else if (resolver instanceof AnnotationCommandMapArgumentResolver) {
					commandMapInx = inx;
				}
			}

			if (commandMapInx != -1) {
				commandMapArgResolver = resolvers.remove(commandMapInx);
				resolvers.add(mapMethodProcessorInx, commandMapArgResolver);
				setArgumentResolvers(resolvers);
			}
		}
	}
}
