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
import org.egovframe.rte.itl.integration.metadata.ServiceDefinition;
import org.egovframe.rte.itl.integration.metadata.SystemDefinition;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 연계 메타데이터 JDBC 저장소 — 표준 테이블(EGOV_ITL_*)에서 정의 그래프를 읽는다.
 *
 * <p>Hibernate 매핑 없이 표준 JDBC 로 연계 정의를 읽는 구현이다. 조회는 {@link EgovInMemoryIntegrationMetadataStore} 의
 * 불변 스냅숏에서 하고, {@link #reload()} 를 부르면 DB 를 다시 읽어 원자적으로 교체한다(재기동 없이 연계 정의 반영).</p>
 *
 * <p>표준 테이블(기존 Hibernate 매핑 테이블과 무관한 별도 스키마, DBMS 에 맞게 타입 조정):</p>
 * <pre>
 * EGOV_ITL_ORGANIZATION (ORGANIZATION_ID PK, ORGANIZATION_NAME)
 * EGOV_ITL_SYSTEM       (SYSTEM_KEY PK, ORGANIZATION_ID, SYSTEM_ID, SYSTEM_NAME, STANDARD_YN)
 * EGOV_ITL_SERVICE      (SERVICE_KEY PK, SYSTEM_KEY, SERVICE_ID, SERVICE_NAME,
 *                        REQUEST_MESSAGE_TYPE_ID, RESPONSE_MESSAGE_TYPE_ID,
 *                        SERVICE_PROVIDER_BEAN_ID, STANDARD_YN, USING_YN)
 * EGOV_ITL_INTEGRATION  (INTEGRATION_ID PK, PROVIDER_SERVICE_KEY, CONSUMER_SYSTEM_KEY,
 *                        DEFAULT_TIMEOUT, USING_YN, VALIDATE_FROM, VALIDATE_TO)
 * </pre>
 *
 * <p>레코드타입 정의는 이 저장소의 범위 밖이다. 필요하면 {@link #reload(java.util.Collection, java.util.Collection,
 * java.util.Collection)} 로 따로 등록한다.</p>
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
public class EgovJdbcIntegrationMetadataStore extends EgovInMemoryIntegrationMetadataStore {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 저장소를 만들고 최초 로딩을 한다.
     *
     * @param dataSource 표준 테이블이 있는 DataSource
     * @throws IllegalArgumentException dataSource 가 null 인 경우
     */
    public EgovJdbcIntegrationMetadataStore(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource must not be null");
        }
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        reload();
    }

    /**
     * 표준 테이블에서 연계 정의 그래프를 다시 읽어 스냅숏을 교체한다.
     */
    public final void reload() {
        Map<String, OrganizationDefinition> organizations = new HashMap<>();
        jdbcTemplate.query("SELECT ORGANIZATION_ID, ORGANIZATION_NAME FROM EGOV_ITL_ORGANIZATION", rs -> {
            OrganizationDefinition organization = new OrganizationDefinition(
                    rs.getString("ORGANIZATION_ID"), rs.getString("ORGANIZATION_NAME"));
            organizations.put(organization.getId(), organization);
        });

        Map<String, SystemDefinition> systemsByKey = new HashMap<>();
        jdbcTemplate.query("SELECT SYSTEM_KEY, ORGANIZATION_ID, SYSTEM_ID, SYSTEM_NAME, STANDARD_YN"
                + " FROM EGOV_ITL_SYSTEM", rs -> {
            OrganizationDefinition organization = organizations.get(rs.getString("ORGANIZATION_ID"));
            SystemDefinition system = new SystemDefinition(rs.getString("SYSTEM_KEY"), organization,
                    rs.getString("SYSTEM_ID"), rs.getString("SYSTEM_NAME"), yn(rs.getString("STANDARD_YN")));
            systemsByKey.put(system.getKey(), system);
            if (organization != null) {
                organization.getSystems().put(system.getId(), system);
            }
        });

        Map<String, ServiceDefinition> servicesByKey = new HashMap<>();
        jdbcTemplate.query("SELECT SERVICE_KEY, SYSTEM_KEY, SERVICE_ID, SERVICE_NAME,"
                + " REQUEST_MESSAGE_TYPE_ID, RESPONSE_MESSAGE_TYPE_ID, SERVICE_PROVIDER_BEAN_ID,"
                + " STANDARD_YN, USING_YN FROM EGOV_ITL_SERVICE", rs -> {
            SystemDefinition system = systemsByKey.get(rs.getString("SYSTEM_KEY"));
            ServiceDefinition service = new ServiceDefinition(rs.getString("SERVICE_KEY"), system,
                    rs.getString("SERVICE_ID"), rs.getString("SERVICE_NAME"),
                    rs.getString("REQUEST_MESSAGE_TYPE_ID"), rs.getString("RESPONSE_MESSAGE_TYPE_ID"),
                    rs.getString("SERVICE_PROVIDER_BEAN_ID"),
                    yn(rs.getString("STANDARD_YN")), yn(rs.getString("USING_YN")));
            servicesByKey.put(service.getKey(), service);
            if (system != null) {
                system.getServices().put(service.getId(), service);
            }
        });

        List<IntegrationDefinition> integrations = new ArrayList<>();
        jdbcTemplate.query("SELECT INTEGRATION_ID, PROVIDER_SERVICE_KEY, CONSUMER_SYSTEM_KEY,"
                + " DEFAULT_TIMEOUT, USING_YN, VALIDATE_FROM, VALIDATE_TO FROM EGOV_ITL_INTEGRATION", rs -> {
            integrations.add(new IntegrationDefinition(rs.getString("INTEGRATION_ID"),
                    servicesByKey.get(rs.getString("PROVIDER_SERVICE_KEY")),
                    systemsByKey.get(rs.getString("CONSUMER_SYSTEM_KEY")),
                    rs.getLong("DEFAULT_TIMEOUT"), yn(rs.getString("USING_YN")),
                    toCalendar(rs.getTimestamp("VALIDATE_FROM")), toCalendar(rs.getTimestamp("VALIDATE_TO"))));
        });

        reload(organizations.values(), integrations, null);
    }

    private static boolean yn(String value) {
        return "Y".equalsIgnoreCase(value);
    }

    private static Calendar toCalendar(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getTime());
        return calendar;
    }

}
