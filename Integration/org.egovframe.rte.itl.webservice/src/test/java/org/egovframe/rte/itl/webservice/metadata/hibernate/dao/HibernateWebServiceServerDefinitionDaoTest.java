package org.egovframe.rte.itl.webservice.metadata.hibernate.dao;

import jakarta.annotation.Resource;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.egovframe.rte.itl.webservice.config.HibernateContextConfig;
import org.egovframe.rte.itl.webservice.data.WebServiceServerDefinition;
import org.egovframe.rte.itl.webservice.data.dao.hibernate.HibernateWebServiceServerDefinitionDao;
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
public class HibernateWebServiceServerDefinitionDaoTest {

    @Resource(name = "hibernateWebServiceServerDefinitionDao")
    private HibernateWebServiceServerDefinitionDao dao;

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
        WebServiceServerDefinition wsd = dao.getWebServiceServerDefinition("3");
        assertNotNull(wsd);
        assertTrue(wsd.isValid());
    }

    @Test
    public void testReadFails() {
        WebServiceServerDefinition wsd = dao.getWebServiceServerDefinition("1");
        assertNull(wsd);
    }

}
