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
package org.egovframe.rte.itl.webservice;

import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;

/**
 * 전자정부 웹 서비스를 Servlet을 통해 제공하기 위해 사용하는 Servlet 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 웹 서비스를 Servlet을 통해 제공하기 위해 사용하는 Servlet class이다.
 * </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * </pre>
 */
public class EgovWebServiceServlet extends CXFNonSpringServlet {

	/**
	 * serialVersion UID
	 */
	private static final long serialVersionUID = -4456525394813473566L;

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovWebServiceServlet.class);

	public void loadBus(ServletConfig servletConfig) {
		super.loadBus(servletConfig);

		LOGGER.debug("EgovWebServiceServlet loadBus");

		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletConfig.getServletContext());
		if (applicationContext == null) {
			LOGGER.error("applicationContext is null");
			return;
		}

		EgovWebServiceContext context = null;

		try {
			context = (EgovWebServiceContext) applicationContext.getBean("egovWebServiceContext", EgovWebServiceContext.class);
		} catch (BeansException e) {
			LOGGER.error("Cannot get EgovWebServiceContext {}", e);
			return;
		}

		context.publishServer(getBus());
	}

}
