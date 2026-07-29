/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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

import jakarta.servlet.ServletConfig;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 전자정부 웹 서비스를 Servlet을 통해 제공하기 위해 사용하는 Servlet 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 웹 서비스를 Servlet을 통해 제공하기 위해 사용하는 Servlet class이다.
 * </p>
 *
 * <p><b>보안 주의:</b> {@link CXFNonSpringServlet}의 기본 동작을 그대로 상속하므로, 게시된 각 서비스는
 * {@code ?wsdl} 쿼리로 WSDL(오퍼레이션명·파라미터 타입 등 서비스 구조)이 별도 인증 없이 노출된다.
 * 내부망 전용으로 의도된 연계 서비스라면 이 서블릿 경로를 네트워크/게이트웨이 수준에서 접근 제어해야
 * 한다(이 클래스 자체는 WSDL 비공개 옵션을 제공하지 않음). 또한 WSDL/XSD 파싱 시 XXE 방어는 이 코드가
 * 아니라 CXF/JAX-WS 런타임의 기본 XML 파서 보안 설정에 의존한다.</p>
 *
 * @author 실행환경 개발팀 심상호
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * </pre>
 * @since 2009.06.01
 */
public class EgovWebServiceServlet extends CXFNonSpringServlet {

    /**
     * serialVersion UID
     */
    private static final long serialVersionUID = -4456525394813473566L;

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovWebServiceServlet.class);

    public void loadBus(ServletConfig servletConfig) {
        super.loadBus(servletConfig);

        LOGGER.debug("### EgovWebServiceServlet loadBus() Start ");

        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletConfig.getServletContext());
        if (applicationContext == null) {
            LOGGER.debug("### EgovWebServiceServlet loadBus() applicationContext is null");
            return;
        }

        EgovWebServiceContext context = null;

        try {
            context = (EgovWebServiceContext) applicationContext.getBean("egovWebServiceContext", EgovWebServiceContext.class);
        } catch (BeansException e) {
            LOGGER.debug("[{}] EgovWebServiceServlet loadBus() Cannot get EgovWebServiceContext {}", e.getClass().getName(), e.getMessage());
            return;
        }

        context.publishServer(getBus());
    }

}
