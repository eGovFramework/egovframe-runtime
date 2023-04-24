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
package org.egovframe.rte.itl.integration.metadata.dao.hibernate;

import org.egovframe.rte.itl.integration.metadata.IntegrationDefinition;
import org.egovframe.rte.itl.integration.metadata.dao.IntegrationDefinitionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.List;

/**
 * 연계 서비스 IntegrationDefinitionDao 구현 클래스.
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 IntegrationDefinitionDao interface 를 Hibernate를 이용하여
 * 구현한 DAO class이다.
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
public class HibernateIntegrationDefinitionDao extends HibernateDaoSupport implements IntegrationDefinitionDao {
	// CHECKSTYLE:OFF
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateIntegrationDefinitionDao.class);

	public IntegrationDefinition getIntegrationDefinition(String id) {
		LOGGER.debug("get IntegrationDefinition (id = \"{}\")", id);

		IntegrationDefinition integrationDefinition = getHibernateTemplate().get(IntegrationDefinition.class, id);
		// CHECKSTYLE:ON
		LOGGER.debug("get IntegrationDefinition (id = \"{}\") = {}", id, integrationDefinition);

		return integrationDefinition;
	}

	@SuppressWarnings("unchecked")
	public List<IntegrationDefinition> getIntegrationDefinitionOfConsumer(String consumerOrganizationId, String consumerSystemId) {
		LOGGER.debug("get IntegrationDefinition of Consumer(organizationId = \"{}\", systemId = \"{}\")", consumerOrganizationId, consumerSystemId);
		// CHECKSTYLE:OFF
		List<IntegrationDefinition> list = (List<IntegrationDefinition>) getHibernateTemplate().find(
				"from IntegrationDefinition as integrationDefinition " + "where integrationDefinition.consumer.organization.id = ?0 "
						+ "and integrationDefinition.consumer.id = ?1 ", new Object[] { consumerOrganizationId, consumerSystemId });
		//	CHECKSTYLE:ON
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("get IntegrationDefinition of Consumer(organizationId = \"{}\", systemId = \"{}\")'s size = {}", consumerOrganizationId, consumerSystemId, list.size());

			int i = 0;
			for (IntegrationDefinition integrationDefinition : list) {
				LOGGER.debug("[{}] : {}", i, integrationDefinition);
				i++;
			}
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	public List<IntegrationDefinition> getIntegrationDefinitionOfProvider(String providerOrganizationId, String providerSystemId) {
		LOGGER.debug("get IntegrationDefinition of Provider(organizationId = \"{}\", systemId = \"{}\")", providerOrganizationId, providerSystemId);
		//		CHECKSTYLE:OFF
		List<IntegrationDefinition> list = (List<IntegrationDefinition>)getHibernateTemplate().find(
				"from IntegrationDefinition as integrationDefinition " + "where integrationDefinition.provider.system.organization.id = ?0 "
						+ "and integrationDefinition.provider.system.id = ?1 ", new Object[] { providerOrganizationId, providerSystemId });
		//		CHECKSTYLE:ON

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("get IntegrationDefinition of Provider(organizationId = \"{}\", systemId = \"{}\")'s size = {}", providerOrganizationId, providerSystemId, list.size());

			int i = 0;
			for (IntegrationDefinition integrationDefinition : list) {
				LOGGER.debug("[{}] : {}", i, integrationDefinition);
				i++;
			}
		}

		return list;
	}
}
