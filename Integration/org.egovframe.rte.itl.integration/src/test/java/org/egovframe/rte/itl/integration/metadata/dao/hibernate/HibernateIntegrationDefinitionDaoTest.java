package org.egovframe.rte.itl.integration.metadata.dao.hibernate;

import jakarta.annotation.Resource;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.egovframe.rte.itl.integration.config.HibernateContextConfig;
import org.egovframe.rte.itl.integration.metadata.IntegrationDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HibernateContextConfig.class)
@Transactional
public class HibernateIntegrationDefinitionDaoTest {

    @Resource(name = "hibernateIntegrationDefinitionDao")
    private HibernateIntegrationDefinitionDao dao;

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
        IntegrationDefinition integrationDefinition = dao.getIntegrationDefinition("1");
        assertNotNull(integrationDefinition);
        assertTrue(integrationDefinition.isValid());
        assertEquals("1", integrationDefinition.getProvider().getKey());
        assertEquals("1", integrationDefinition.getConsumer().getKey());
        assertTrue(integrationDefinition.isUsing());
        assertEquals(2009, integrationDefinition.getValidateFrom().get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, integrationDefinition.getValidateFrom().get(Calendar.MONTH));
        assertEquals(1, integrationDefinition.getValidateFrom().get(Calendar.DAY_OF_MONTH));
        assertEquals(0, integrationDefinition.getValidateFrom().get(Calendar.HOUR_OF_DAY));
        assertEquals(0, integrationDefinition.getValidateFrom().get(Calendar.MINUTE));
        assertEquals(0, integrationDefinition.getValidateFrom().get(Calendar.SECOND));
        assertEquals(2009, integrationDefinition.getValidateTo().get(Calendar.YEAR));
        assertEquals(Calendar.MAY, integrationDefinition.getValidateTo().get(Calendar.MONTH));
        assertEquals(31, integrationDefinition.getValidateTo().get(Calendar.DAY_OF_MONTH));
        assertEquals(23, integrationDefinition.getValidateTo().get(Calendar.HOUR_OF_DAY));
        assertEquals(59, integrationDefinition.getValidateTo().get(Calendar.MINUTE));
        assertEquals(59, integrationDefinition.getValidateTo().get(Calendar.SECOND));
    }

    @Test
    public void testReadOfConsumerSucceeds() {
        List<IntegrationDefinition> list = dao.getIntegrationDefinitionOfConsumer("00000000", "00000000");
        assertEquals(2, list.size());
        assertEquals("1", list.get(0).getId());
        assertTrue(list.get(0).isValid());
        assertEquals("2", list.get(1).getId());
        assertTrue(list.get(1).isValid());
    }

    @Test
    public void testReadOfProviderSucceeds() {
        List<IntegrationDefinition> list = dao.getIntegrationDefinitionOfProvider("00000000", "00000000");
        assertEquals(1, list.size());
        assertEquals("1", list.get(0).getId());
        assertTrue(list.get(0).isValid());
    }

    @Test
    public void testReadFails() {
        IntegrationDefinition integrationRegistry = dao.getIntegrationDefinition("0");
        assertNull(integrationRegistry);
    }

}
