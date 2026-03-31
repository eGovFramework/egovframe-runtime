package org.egovframe.rte.itl.integration.metadata.dao.hibernate;

import jakarta.annotation.Resource;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.egovframe.rte.itl.integration.config.HibernateContextConfig;
import org.egovframe.rte.itl.integration.metadata.ServiceDefinition;
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
public class HibernateServiceDefinitionDaoTest {

    @Resource(name = "hibernateServiceDefinitionDao")
    private HibernateServiceDefinitionDao dao;

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
        ServiceDefinition serviceA0_0 = dao.getServiceDefinition("00000000", "00000000", "00000000");
        assertNotNull(serviceA0_0);
        assertTrue(serviceA0_0.isValid());
        assertEquals("00000000", serviceA0_0.getId());
        assertEquals("Service A0-0", serviceA0_0.getName());
        assertEquals("M1", serviceA0_0.getRequestMessageTypeId());
        assertEquals("M2", serviceA0_0.getResponseMessageTypeId());
        assertNull(serviceA0_0.getServiceProviderBeanId());
        assertEquals("00000000", serviceA0_0.getSystem().getId());
    }

    @Test
    public void testReadFails() {
        ServiceDefinition service = dao.getServiceDefinition("00000000", "00000000", "00000003");
        assertNull(service);
    }

}
