package org.egovframe.rte.itl.integration.metadata.store;

import org.egovframe.rte.itl.integration.metadata.IntegrationDefinition;
import org.egovframe.rte.itl.integration.metadata.ServiceDefinition;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovJdbcIntegrationMetadataStore 의 표준 테이블 로딩, 그래프 연결, 재기동 없는 reload 를 HSQLDB 로 검증한다.
 */
public class EgovJdbcIntegrationMetadataStoreTest {

    private static JDBCDataSource dataSource;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        dataSource = new JDBCDataSource();
        dataSource.setURL("jdbc:hsqldb:mem:egovitlmeta");
        dataSource.setUser("sa");
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE EGOV_ITL_INTEGRATION IF EXISTS");
            statement.execute("DROP TABLE EGOV_ITL_SERVICE IF EXISTS");
            statement.execute("DROP TABLE EGOV_ITL_SYSTEM IF EXISTS");
            statement.execute("DROP TABLE EGOV_ITL_ORGANIZATION IF EXISTS");
            statement.execute("CREATE TABLE EGOV_ITL_ORGANIZATION ("
                    + "ORGANIZATION_ID VARCHAR(20) PRIMARY KEY, ORGANIZATION_NAME VARCHAR(100))");
            statement.execute("CREATE TABLE EGOV_ITL_SYSTEM (SYSTEM_KEY VARCHAR(40) PRIMARY KEY,"
                    + " ORGANIZATION_ID VARCHAR(20), SYSTEM_ID VARCHAR(20), SYSTEM_NAME VARCHAR(100),"
                    + " STANDARD_YN CHAR(1))");
            statement.execute("CREATE TABLE EGOV_ITL_SERVICE (SERVICE_KEY VARCHAR(40) PRIMARY KEY,"
                    + " SYSTEM_KEY VARCHAR(40), SERVICE_ID VARCHAR(20), SERVICE_NAME VARCHAR(100),"
                    + " REQUEST_MESSAGE_TYPE_ID VARCHAR(40), RESPONSE_MESSAGE_TYPE_ID VARCHAR(40),"
                    + " SERVICE_PROVIDER_BEAN_ID VARCHAR(60), STANDARD_YN CHAR(1), USING_YN CHAR(1))");
            statement.execute("CREATE TABLE EGOV_ITL_INTEGRATION (INTEGRATION_ID VARCHAR(20) PRIMARY KEY,"
                    + " PROVIDER_SERVICE_KEY VARCHAR(40), CONSUMER_SYSTEM_KEY VARCHAR(40),"
                    + " DEFAULT_TIMEOUT BIGINT, USING_YN CHAR(1), VALIDATE_FROM TIMESTAMP, VALIDATE_TO TIMESTAMP)");
            statement.execute("INSERT INTO EGOV_ITL_ORGANIZATION VALUES ('ORG1', '행정안전부')");
            statement.execute("INSERT INTO EGOV_ITL_ORGANIZATION VALUES ('ORG2', '국세청')");
            statement.execute("INSERT INTO EGOV_ITL_SYSTEM VALUES ('SYS-KEY-1', 'ORG1', 'SYS1', '인사시스템', 'Y')");
            statement.execute("INSERT INTO EGOV_ITL_SYSTEM VALUES ('SYS-KEY-2', 'ORG2', 'SYS2', '세정시스템', 'N')");
            statement.execute("INSERT INTO EGOV_ITL_SERVICE VALUES ('SVC-KEY-1', 'SYS-KEY-1', 'SVC1',"
                    + " '직원조회', 'reqType', 'resType', 'employeeServiceProvider', 'Y', 'Y')");
            statement.execute("INSERT INTO EGOV_ITL_INTEGRATION VALUES ('INT-001', 'SVC-KEY-1', 'SYS-KEY-2',"
                    + " 5000, 'Y', TIMESTAMP '2026-01-01 00:00:00', NULL)");
        }
    }

    @Test
    public void testLoadsDefinitionGraphFromStandardTables() {
        EgovJdbcIntegrationMetadataStore store = new EgovJdbcIntegrationMetadataStore(dataSource);

        ServiceDefinition service = store.getServiceDefinition("ORG1", "SYS1", "SVC1");
        assertNotNull(service);
        assertEquals("직원조회", service.getName());
        assertEquals("employeeServiceProvider", service.getServiceProviderBeanId());
        assertEquals("ORG1", service.getSystem().getOrganization().getId());
        assertTrue(service.isStandard());

        IntegrationDefinition integration = store.getIntegrationDefinition("INT-001");
        assertNotNull(integration);
        assertEquals(5000, integration.getDefaultTimeout());
        assertTrue(integration.isUsing());
        assertNotNull(integration.getValidateFrom());

        List<IntegrationDefinition> ofConsumer = store.getIntegrationDefinitionOfConsumer("ORG2", "SYS2");
        assertEquals(1, ofConsumer.size());
        assertEquals(1, store.getIntegrationDefinitionOfProvider("ORG1", "SYS1").size());
    }

    @Test
    public void testReloadReflectsChangedDefinitionsWithoutRestart() throws Exception {
        EgovJdbcIntegrationMetadataStore store = new EgovJdbcIntegrationMetadataStore(dataSource);
        assertEquals("직원조회", store.getServiceDefinition("SVC-KEY-1").getName());
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE EGOV_ITL_SERVICE SET SERVICE_NAME = '직원조회v2' WHERE SERVICE_KEY = 'SVC-KEY-1'");
        }

        store.reload();

        assertEquals("직원조회v2", store.getServiceDefinition("SVC-KEY-1").getName());
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE EGOV_ITL_SERVICE SET SERVICE_NAME = '직원조회' WHERE SERVICE_KEY = 'SVC-KEY-1'");
        }
    }

    @Test
    public void testNullDataSourceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EgovJdbcIntegrationMetadataStore(null));
    }

}
