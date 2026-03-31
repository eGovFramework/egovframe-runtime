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
import org.egovframe.rte.itl.integration.metadata.IntegrationDefinition;
import org.egovframe.rte.itl.integration.metadata.dao.IntegrationDefinitionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 연계 서비스 IntegrationDefinitionDao 구현 클래스.
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 IntegrationDefinitionDao interface 를 Hibernate를 이용하여
 * 구현한 DAO class이다.
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
public class HibernateIntegrationDefinitionDao implements IntegrationDefinitionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateIntegrationDefinitionDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public IntegrationDefinition getIntegrationDefinition(String id) {
        return entityManager.find(IntegrationDefinition.class, id);
    }

    @Override
    public List<IntegrationDefinition> getIntegrationDefinitionOfConsumer(String consumerOrgId, String consumerSystemId) {
        return entityManager.createQuery("""
                        SELECT i FROM IntegrationDefinition i WHERE i.consumer.organization.id = :orgId AND i.consumer.id = :systemId
                        """, IntegrationDefinition.class)
                .setParameter("orgId", consumerOrgId)
                .setParameter("systemId", consumerSystemId)
                .getResultList();
    }

    @Override
    public List<IntegrationDefinition> getIntegrationDefinitionOfProvider(String providerOrgId, String providerSystemId) {
        return entityManager.createQuery("""
                        SELECT i FROM IntegrationDefinition i WHERE i.provider.system.organization.id = :orgId AND i.provider.system.id = :systemId
                        """, IntegrationDefinition.class)
                .setParameter("orgId", providerOrgId)
                .setParameter("systemId", providerSystemId)
                .getResultList();
    }

}
