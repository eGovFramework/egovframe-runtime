/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.itl.integration.metadata.store;

import org.egovframe.rte.itl.integration.metadata.IntegrationDefinition;
import org.egovframe.rte.itl.integration.metadata.OrganizationDefinition;
import org.egovframe.rte.itl.integration.metadata.RecordTypeDefinition;
import org.egovframe.rte.itl.integration.metadata.ServiceDefinition;
import org.egovframe.rte.itl.integration.metadata.SystemDefinition;
import org.egovframe.rte.itl.integration.metadata.dao.IntegrationDefinitionDao;
import org.egovframe.rte.itl.integration.metadata.dao.OrganizationDefinitionDao;
import org.egovframe.rte.itl.integration.metadata.dao.RecordTypeDefinitionDao;
import org.egovframe.rte.itl.integration.metadata.dao.ServiceDefinitionDao;
import org.egovframe.rte.itl.integration.metadata.dao.SystemDefinitionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 연계 메타데이터 InMemory 저장소 — DAO 5종 계약을 Hibernate 없이 한 클래스로 구현한다.
 *
 * <p>연계 정의(기관→시스템→서비스 그래프, 연계, 레코드타입)를 프로그램이나 설정으로 등록해 쓴다. 메타데이터 로딩에 ORM 을
 * 요구하는 {@code dao.hibernate.*} 의 대체 구현이며, 다른 저장소(JDBC 등)는
 * {@link #reload(Collection, Collection, Collection)} 로 스냅숏을 교체하는 방식으로 이 클래스를 확장한다.</p>
 *
 * <p><b>불변 스냅숏 교체</b>: 조회는 volatile 스냅숏(불변 인덱스)에서 잠금 없이 하고, reload 는 새 인덱스를 조립해
 * 원자적으로 교체한다. 조회 중인 스레드는 이전 스냅숏을, 이후 스레드는 새 스냅숏을 본다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public class EgovInMemoryIntegrationMetadataStore implements OrganizationDefinitionDao,
        SystemDefinitionDao, ServiceDefinitionDao, IntegrationDefinitionDao, RecordTypeDefinitionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovInMemoryIntegrationMetadataStore.class);

    private static final String KEY_SEPARATOR = " ";

    /** 불변 조회 인덱스 스냅숏 */
    private static final class Snapshot {
        private static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), Map.of(),
                Map.of(), Map.of(), Map.of(), Map.of(), List.of(), Map.of());

        private final Map<String, OrganizationDefinition> organizationsById;
        private final Map<String, SystemDefinition> systemsByKey;
        private final Map<String, SystemDefinition> systemsByOrgAndId;
        private final Map<String, ServiceDefinition> servicesByKey;
        private final Map<String, ServiceDefinition> servicesBySystemKeyAndId;
        private final Map<String, ServiceDefinition> servicesByOrgSystemService;
        private final Map<String, IntegrationDefinition> integrationsById;
        private final List<IntegrationDefinition> integrations;
        private final Map<String, RecordTypeDefinition> recordTypesById;

        private Snapshot(Map<String, OrganizationDefinition> organizationsById,
                Map<String, SystemDefinition> systemsByKey,
                Map<String, SystemDefinition> systemsByOrgAndId,
                Map<String, ServiceDefinition> servicesByKey,
                Map<String, ServiceDefinition> servicesBySystemKeyAndId,
                Map<String, ServiceDefinition> servicesByOrgSystemService,
                Map<String, IntegrationDefinition> integrationsById,
                List<IntegrationDefinition> integrations,
                Map<String, RecordTypeDefinition> recordTypesById) {
            this.organizationsById = Collections.unmodifiableMap(organizationsById);
            this.systemsByKey = Collections.unmodifiableMap(systemsByKey);
            this.systemsByOrgAndId = Collections.unmodifiableMap(systemsByOrgAndId);
            this.servicesByKey = Collections.unmodifiableMap(servicesByKey);
            this.servicesBySystemKeyAndId = Collections.unmodifiableMap(servicesBySystemKeyAndId);
            this.servicesByOrgSystemService = Collections.unmodifiableMap(servicesByOrgSystemService);
            this.integrationsById = Collections.unmodifiableMap(integrationsById);
            this.integrations = Collections.unmodifiableList(integrations);
            this.recordTypesById = Collections.unmodifiableMap(recordTypesById);
        }
    }

    private volatile Snapshot snapshot = Snapshot.EMPTY;

    /**
     * 연계 정의로 조회 인덱스를 새로 조립해 원자적으로 교체한다.
     *
     * @param organizations 기관 정의(소속 시스템·서비스 그래프 포함, null 허용)
     * @param integrations  연계 정의(null 허용)
     * @param recordTypes   레코드타입 정의(null 허용)
     */
    public final void reload(Collection<OrganizationDefinition> organizations,
            Collection<IntegrationDefinition> integrations,
            Collection<RecordTypeDefinition> recordTypes) {
        Map<String, OrganizationDefinition> organizationsById = new HashMap<>();
        Map<String, SystemDefinition> systemsByKey = new HashMap<>();
        Map<String, SystemDefinition> systemsByOrgAndId = new HashMap<>();
        Map<String, ServiceDefinition> servicesByKey = new HashMap<>();
        Map<String, ServiceDefinition> servicesBySystemKeyAndId = new HashMap<>();
        Map<String, ServiceDefinition> servicesByOrgSystemService = new HashMap<>();
        if (organizations != null) {
            for (OrganizationDefinition organization : organizations) {
                organizationsById.put(organization.getId(), organization);
                for (SystemDefinition system : organization.getSystems().values()) {
                    systemsByKey.put(system.getKey(), system);
                    systemsByOrgAndId.put(key(organization.getId(), system.getId()), system);
                    for (ServiceDefinition service : system.getServices().values()) {
                        servicesByKey.put(service.getKey(), service);
                        servicesBySystemKeyAndId.put(key(system.getKey(), service.getId()), service);
                        servicesByOrgSystemService.put(
                                key(organization.getId(), system.getId(), service.getId()), service);
                    }
                }
            }
        }
        Map<String, IntegrationDefinition> integrationsById = new HashMap<>();
        List<IntegrationDefinition> integrationList = new ArrayList<>();
        if (integrations != null) {
            for (IntegrationDefinition integration : integrations) {
                integrationsById.put(integration.getId(), integration);
                integrationList.add(integration);
            }
        }
        Map<String, RecordTypeDefinition> recordTypesById = new HashMap<>();
        if (recordTypes != null) {
            for (RecordTypeDefinition recordType : recordTypes) {
                recordTypesById.put(recordType.getId(), recordType);
            }
        }
        this.snapshot = new Snapshot(organizationsById, systemsByKey, systemsByOrgAndId,
                servicesByKey, servicesBySystemKeyAndId, servicesByOrgSystemService,
                integrationsById, integrationList, recordTypesById);
        LOGGER.info("Integration metadata snapshot replaced: organizations={}, systems={}, services={}, integrations={}",
                organizationsById.size(), systemsByKey.size(), servicesByKey.size(), integrationList.size());
    }

    private static String key(String... parts) {
        return String.join(KEY_SEPARATOR, parts);
    }

    @Override
    public OrganizationDefinition getOrganizationDefinition(String id) {
        return snapshot.organizationsById.get(id);
    }

    @Override
    public SystemDefinition getSystemDefinition(String systemKey) {
        return snapshot.systemsByKey.get(systemKey);
    }

    @Override
    public SystemDefinition getSystemDefinition(String organizationId, String systemId) {
        return snapshot.systemsByOrgAndId.get(key(organizationId, systemId));
    }

    @Override
    public ServiceDefinition getServiceDefinition(String serviceKey) {
        return snapshot.servicesByKey.get(serviceKey);
    }

    @Override
    public ServiceDefinition getServiceDefinition(String systemKey, String serviceId) {
        return snapshot.servicesBySystemKeyAndId.get(key(systemKey, serviceId));
    }

    @Override
    public ServiceDefinition getServiceDefinition(String organizationId, String systemId, String serviceId) {
        return snapshot.servicesByOrgSystemService.get(key(organizationId, systemId, serviceId));
    }

    @Override
    public IntegrationDefinition getIntegrationDefinition(String id) {
        return snapshot.integrationsById.get(id);
    }

    @Override
    public List<IntegrationDefinition> getIntegrationDefinitionOfConsumer(String consumerOrganizationId,
            String consumerSystemId) {
        List<IntegrationDefinition> result = new ArrayList<>();
        for (IntegrationDefinition integration : snapshot.integrations) {
            SystemDefinition consumer = integration.getConsumer();
            if (consumer != null && consumer.getOrganization() != null
                    && consumer.getOrganization().getId().equals(consumerOrganizationId)
                    && consumer.getId().equals(consumerSystemId)) {
                result.add(integration);
            }
        }
        return result;
    }

    @Override
    public List<IntegrationDefinition> getIntegrationDefinitionOfProvider(String providerOrganizationId,
            String providerSystemId) {
        List<IntegrationDefinition> result = new ArrayList<>();
        for (IntegrationDefinition integration : snapshot.integrations) {
            ServiceDefinition provider = integration.getProvider();
            SystemDefinition providerSystem = (provider != null) ? provider.getSystem() : null;
            if (providerSystem != null && providerSystem.getOrganization() != null
                    && providerSystem.getOrganization().getId().equals(providerOrganizationId)
                    && providerSystem.getId().equals(providerSystemId)) {
                result.add(integration);
            }
        }
        return result;
    }

    @Override
    public RecordTypeDefinition getRecordTypeDefinition(String id) {
        return snapshot.recordTypesById.get(id);
    }

}
