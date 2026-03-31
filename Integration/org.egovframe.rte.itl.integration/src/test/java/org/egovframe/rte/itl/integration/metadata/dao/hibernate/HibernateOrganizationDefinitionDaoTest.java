package org.egovframe.rte.itl.integration.metadata.dao.hibernate;

import jakarta.annotation.Resource;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.egovframe.rte.itl.integration.config.HibernateContextConfig;
import org.egovframe.rte.itl.integration.metadata.OrganizationDefinition;
import org.egovframe.rte.itl.integration.metadata.ServiceDefinition;
import org.egovframe.rte.itl.integration.metadata.SystemDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HibernateContextConfig.class)
@Transactional
public class HibernateOrganizationDefinitionDaoTest {

    @Resource(name = "hibernateOrganizationDefinitionDao")
    private HibernateOrganizationDefinitionDao dao;

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @BeforeEach
    public void onSetUp() throws Exception {
        // 리소스 파일 로드
        InputStream inputStream = new ClassPathResource("META-INF/spring/dataset.xml").getInputStream();

        // Flat XML → ReplacementDataSet 변환
        IDataSet baseDataSet = new FlatXmlDataSetBuilder().build(inputStream);
        ReplacementDataSet dataSet = new ReplacementDataSet(baseDataSet);
        dataSet.addReplacementObject("[null]", null);

        // DBUnit 연결 생성
        IDatabaseConnection connection = new org.dbunit.database.DatabaseDataSourceConnection(dataSource);

        // 데이터 삽입
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    @Test
    public void testReadSucceeds() {
        OrganizationDefinition organization00000000 = dao.getOrganizationDefinition("00000000");
        assertNotNull(organization00000000);
        assertTrue(organization00000000.isValid());
        assertEquals("00000000", organization00000000.getId());
        assertEquals("Organization A", organization00000000.getName());
        assertEquals(2, organization00000000.getSystems().size());

        SystemDefinition systemA0 = organization00000000.getSystemDefinition("00000000");
        assertNotNull(systemA0);
        assertTrue(systemA0.isValid());
        assertEquals("00000000", systemA0.getId());
        assertEquals("System A0", systemA0.getName());
        assertTrue(systemA0.isStandard());
        assertEquals(1, systemA0.getServices().size());
        assertEquals(organization00000000, systemA0.getOrganization());

        ServiceDefinition serviceA0_0 = systemA0.getServiceDefinition("00000000");
        assertNotNull(serviceA0_0);
        assertTrue(serviceA0_0.isValid());
        assertEquals("00000000", serviceA0_0.getId());
        assertEquals("Service A0-0", serviceA0_0.getName());
        assertEquals("M1", serviceA0_0.getRequestMessageTypeId());
        assertEquals("M2", serviceA0_0.getResponseMessageTypeId());
        assertNull(serviceA0_0.getServiceProviderBeanId());
        assertEquals(systemA0, serviceA0_0.getSystem());

        OrganizationDefinition organization00000001 = dao.getOrganizationDefinition("00000001");
        assertNotNull(organization00000001);
        assertTrue(organization00000001.isValid());
        assertEquals("00000001", organization00000001.getId());
        assertEquals("Organization B", organization00000001.getName());
        assertEquals(2, organization00000000.getSystems().size());
    }

    @Test
    public void testReadFails() {
        OrganizationDefinition organization00000002 = dao.getOrganizationDefinition("00000002");
        assertNull(organization00000002);
    }

}
