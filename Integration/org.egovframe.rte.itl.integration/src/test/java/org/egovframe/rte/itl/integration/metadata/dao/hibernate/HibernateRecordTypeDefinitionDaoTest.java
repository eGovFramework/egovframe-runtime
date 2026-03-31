package org.egovframe.rte.itl.integration.metadata.dao.hibernate;

import jakarta.annotation.Resource;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.egovframe.rte.itl.integration.config.HibernateContextConfig;
import org.egovframe.rte.itl.integration.metadata.RecordTypeDefinition;
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
public class HibernateRecordTypeDefinitionDaoTest {

    @Resource(name = "hibernateRecordTypeDefinitionDao")
    private HibernateRecordTypeDefinitionDao dao;

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
        RecordTypeDefinition rtd0 = dao.getRecordTypeDefinition("M1");
        assertNotNull(rtd0);
        assertTrue(rtd0.isValid());
        assertEquals("M1", rtd0.getId());
        assertNull(rtd0.getParent());
        assertEquals(3, rtd0.getFields().size());
        assertEquals("string", rtd0.getField("a").getTypeId());
        assertEquals("integer", rtd0.getField("b").getTypeId());
        assertEquals("boolean", rtd0.getField("c").getTypeId());

        RecordTypeDefinition rtd1 = dao.getRecordTypeDefinition("M2");
        assertNotNull(rtd1);
        assertTrue(rtd1.isValid());
        assertEquals("M2", rtd1.getId());
        assertNull(rtd1.getParent());
        assertEquals(2, rtd1.getFields().size(), 2);
        assertEquals("string", rtd1.getField("d").getTypeId());
        assertEquals("string", rtd1.getField("e").getTypeId());

        RecordTypeDefinition rtd2 = dao.getRecordTypeDefinition("M3");
        assertNotNull(rtd2);
        assertTrue(rtd2.isValid());
        assertEquals("M3", rtd2.getId());
        assertEquals(rtd1, rtd2.getParent());
        assertEquals(2, rtd2.getFields().size(), 2);
        assertEquals("integer", rtd2.getField("f").getTypeId());
        assertEquals("integer", rtd2.getField("g").getTypeId());

        RecordTypeDefinition rtd3 = dao.getRecordTypeDefinition("M4");
        assertNotNull(rtd3);
        assertTrue(rtd3.isValid());
        assertEquals("M4", rtd3.getId());
        assertEquals(rtd1, rtd3.getParent());
        assertEquals(2, rtd3.getFields().size());
        assertEquals("integer", rtd3.getField("d").getTypeId());
        assertEquals("integer", rtd3.getField("f").getTypeId());
    }

    @Test
    public void testReadFails() {
        RecordTypeDefinition rtd = dao.getRecordTypeDefinition("x");
        assertNull(rtd);
    }

}
