package org.egovframe.rte.itl.webservice.data.dao.hibernate;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.egovframe.rte.itl.webservice.data.WebServiceClientDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/org/egovframe/rte/itl/webservice/data/dao/hibernate/context.xml")
@Transactional(readOnly=false)
public class HibernateWebServiceClientDefinitionDaoTest
{
    @Autowired
    private HibernateWebServiceClientDefinitionDao dao;

    @Autowired
    private DataSource dataSource;
    
    @SuppressWarnings("deprecation")
	@Before
    public void before() throws Exception
    {
        ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
                ResourceUtils.getFile("classpath:org/egovframe/rte/itl/webservice/data/dao/hibernate/dataset.xml")));
        dataSet.addReplacementObject("[null]", null);
        
        IDatabaseConnection connection = new SpringDatabaseDataSourceConnection(dataSource);
        
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    @Test
    public void testReadSucceeds() throws Exception
    {
        WebServiceClientDefinition wsd = dao.getWebServiceClientDefinition("4");
        
        assertNotNull(wsd);
        assertTrue(wsd.isValid());
    }

    @Test
    public void testReadFails() throws Exception
    {
        WebServiceClientDefinition wsd = dao.getWebServiceClientDefinition("1");
        
        assertNull(wsd);
    }
}
