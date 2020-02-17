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
package egovframework.rte.itl.webservice.data.dao.hibernate;

import egovframework.rte.itl.integration.metadata.ServiceDefinition;
import egovframework.rte.itl.webservice.data.WebServiceClientDefinition;
import egovframework.rte.itl.webservice.data.dao.WebServiceClientDefinitionDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * WebServiceClientDefinitionDao를 hibernate를 이용하여 구현한 DAO 클래스
 * <p>
 * <b>NOTE:</b> WebServiceClientDefinitionDao를 hibernate를 이용하여 구현한 DAO class이다. </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 * 
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.06.01  심상호           최초 생성
 *
 * </pre>
 */
public class HibernateWebServiceClientDefinitionDao extends HibernateDaoSupport
		implements WebServiceClientDefinitionDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateWebServiceClientDefinitionDao.class);

	public WebServiceClientDefinition getWebServiceClientDefinition(
			ServiceDefinition serviceDefinition) {
		LOGGER.debug("get WebServiceClientDefinition(serviceDefinition = {})",
				serviceDefinition);

		WebServiceClientDefinition webServiceClientDefinition = getWebServiceClientDefinition(serviceDefinition.getKey());

		LOGGER.debug(
				"get WebServiceClientDefinition(serviceDefinition = {}) = {}",
				serviceDefinition, webServiceClientDefinition);

		return webServiceClientDefinition;
	}

	public WebServiceClientDefinition getWebServiceClientDefinition(String key) {
		LOGGER.debug("get WebServiceClientDefinition(key = \"{}\")", key);
		// CHECKSTYLE:OFF
		WebServiceClientDefinition webServiceClientDefinition = (WebServiceClientDefinition) getHibernateTemplate().get(WebServiceClientDefinition.class, key);
		// CHECKSTYLE:ON
		LOGGER.debug("get WebServiceClientDefinition(key = \"{}\") = {}", key,
				webServiceClientDefinition);

		return webServiceClientDefinition;
	}
}
