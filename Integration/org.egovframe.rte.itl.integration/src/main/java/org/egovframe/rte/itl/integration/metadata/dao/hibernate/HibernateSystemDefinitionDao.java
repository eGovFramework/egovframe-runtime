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
import org.egovframe.rte.itl.integration.metadata.SystemDefinition;
import org.egovframe.rte.itl.integration.metadata.dao.SystemDefinitionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 연계 서비스 SystemDefinitionDao 구현 클래스.
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 SystemDefinitionDao interface 를 Hibernate를 이용하여 구현한
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
public class HibernateSystemDefinitionDao implements SystemDefinitionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateSystemDefinitionDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SystemDefinition getSystemDefinition(String key) {
        return entityManager.find(SystemDefinition.class, key);
    }

    @Override
    public SystemDefinition getSystemDefinition(String organizationId, String systemId) {
        List<SystemDefinition> result = entityManager.createQuery("""
                        SELECT s FROM SystemDefinition s WHERE s.organization.id = :orgId AND s.id = :systemId
                        """, SystemDefinition.class)
                .setParameter("orgId", organizationId)
                .setParameter("systemId", systemId)
                .getResultList();

        if (ObjectUtils.isEmpty(result)) return null;

        if (result.size() > 1) {
            LOGGER.debug("Warning: Multiple SystemDefinition results for {}", organizationId + "/" + systemId);
        }

        return result.get(0);
    }

}
