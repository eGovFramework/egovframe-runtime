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
package egovframework.rte.itl.integration.metadata.dao.hibernate;

import java.util.List;

import egovframework.rte.itl.integration.metadata.IntegrationDefinition;
import egovframework.rte.itl.integration.metadata.ServiceDefinition;
import egovframework.rte.itl.integration.metadata.dao.ServiceDefinitionDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/**
 * 연계 서비스 ServiceDefinitionDao 구현 클래스.
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 ServiceDefinitionDao interface 를 Hibernate를 이용하여 구현한
 * DAO class이다.
 * </p>
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
public class HibernateServiceDefinitionDao extends HibernateDaoSupport implements ServiceDefinitionDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateServiceDefinitionDao.class);

	public ServiceDefinition getServiceDefinition(String key) {
		LOGGER.debug("get ServiceDefinition(key = \"{}\")", key);

		ServiceDefinition serviceDefinition = (ServiceDefinition) getHibernateTemplate().get(ServiceDefinition.class, key);

		LOGGER.debug("get ServiceDefinition(key = \"{}\") = {}", key, serviceDefinition);

		return serviceDefinition;
	}


	@SuppressWarnings("unchecked")
	public ServiceDefinition getServiceDefinition(String systemKey, String serviceId) {
		LOGGER.debug("get ServiceDefinition(systemKey = \"{}\", serviceId = \"{}\")", systemKey, serviceId);

		ServiceDefinition serviceDefinition = null;

		List<ServiceDefinition> result = (List<ServiceDefinition>)getHibernateTemplate().find(
				"from ServiceDefinition as service " + "where service.system.key = ?0 "
						+ "and service.id = ?1", new Object[] { systemKey, serviceId });
		if (result != null && result.size() > 0) {
			serviceDefinition = result.get(0);
			if (result.size() != 1) {
				LOGGER.info("get ServiceDefinition(systemKey = \"{}\", serviceId = \"{}\")'s size is not 1 ({})", systemKey, serviceId, result.size());
			}
		}

		LOGGER.debug("get ServiceDefinition(systemKey = \"{}\", serviceId = \"{}\") = {}", systemKey, serviceId, serviceDefinition);

		return serviceDefinition;
	}

	@SuppressWarnings("unchecked")
	public ServiceDefinition getServiceDefinition(String organizationId, String systemId, String serviceId) {
		LOGGER.debug("get ServiceDefinition(organizationId = \"{}\", systemId = \"{}\", serviceId = \"{}\")", organizationId, systemId, serviceId);

		ServiceDefinition serviceDefinition = null;

		List<ServiceDefinition> result = (List<ServiceDefinition>)getHibernateTemplate().find(
				"from ServiceDefinition as service " + "where service.system.organization.id = ?0 "
						+ "and service.system.id = ?1 " + "and service.id = ?2 ", new Object[] { organizationId, systemId, serviceId });
		if (result != null && result.size() > 0) {
			serviceDefinition = result.get(0);
			if (result.size() != 1) {
				LOGGER.debug("get ServiceDefinition(organizationId = \"{}\", systemId = \"{}\", serviceId = \"{}\")'size is not 1 ({})", organizationId, systemId, serviceId, result.size());
			}
		}

		LOGGER.debug("get ServiceDefinition(organizationId = \"{}\", systemId = \"{}\", serviceId = \"{}\") = {}", organizationId, systemId, serviceId, serviceDefinition);

		return serviceDefinition;
	}

}
