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
package org.egovframe.rte.itl.integration.metadata.dao.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.egovframe.rte.itl.integration.metadata.ServiceDefinition;
import org.egovframe.rte.itl.integration.metadata.dao.ServiceDefinitionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 연계 서비스 ServiceDefinitionDao 구현 클래스.
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 ServiceDefinitionDao interface 를 Hibernate를 이용하여 구현한
 * DAO class이다.
 * </p>
 *
 * @author 실행환경 개발팀 심상호
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.06.01  심상호           최초 생성
 *
 * </pre>
 * @since 2009.06.01
 */
public class HibernateServiceDefinitionDao implements ServiceDefinitionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateServiceDefinitionDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ServiceDefinition getServiceDefinition(String key) {
        return entityManager.find(ServiceDefinition.class, key);
    }

    @Override
    public ServiceDefinition getServiceDefinition(String systemKey, String serviceId) {
        List<ServiceDefinition> result = entityManager.createQuery("""
                        SELECT s FROM ServiceDefinition s WHERE s.system.key = :systemKey AND s.id = :serviceId
                        """, ServiceDefinition.class)
                .setParameter("systemKey", systemKey)
                .setParameter("serviceId", serviceId)
                .getResultList();

        return handleSingleResult(result, systemKey + "/" + serviceId);
    }

    @Override
    public ServiceDefinition getServiceDefinition(String organizationId, String systemId, String serviceId) {
        List<ServiceDefinition> result = entityManager.createQuery("""
                        SELECT s FROM ServiceDefinition s WHERE s.system.organization.id = :orgId AND s.system.id = :systemId AND s.id = :serviceId
                        """, ServiceDefinition.class)
                .setParameter("orgId", organizationId)
                .setParameter("systemId", systemId)
                .setParameter("serviceId", serviceId)
                .getResultList();

        return handleSingleResult(result, organizationId + "/" + systemId + "/" + serviceId);
    }

    private ServiceDefinition handleSingleResult(List<ServiceDefinition> result, String context) {
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }

        if (result.size() > 1) {
            LOGGER.debug("Warning: Multiple results found for {}", context);
        }

        return result.get(0);
    }

}
