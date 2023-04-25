package org.egovframe.rte.itl.integration.metadata.dao.hibernate;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.egovframe.rte.itl.integration.metadata.ServiceDefinition;
import org.egovframe.rte.itl.integration.metadata.SystemDefinition;
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
@ContextConfiguration(locations = "/org/egovframe/rte/itl/integration/metadata/dao/hibernate/context.xml")
@Transactional(readOnly=false)
public class HibernateSystemDefinitionDaoTest
{
    @Autowired
    private HibernateSystemDefinitionDao dao;

    @Autowired
    private DataSource dataSource;
    
    @SuppressWarnings("deprecation")
	@Before
    public void before() throws Exception
    {
        ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
                ResourceUtils.getFile("classpath:org/egovframe/rte/itl/integration/metadata/dao/hibernate/dataset.xml")));
        dataSet.addReplacementObject("[null]", null);
        
        IDatabaseConnection connection = new SpringDatabaseDataSourceConnection(dataSource);
        
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }
    
    @Test
    public void testReadSucceeds() throws Exception
    {
        SystemDefinition systemA0 = dao.getSystemDefinition("00000000", "00000000");
        assertNotNull(systemA0);
        assertTrue(systemA0.isValid());
        assertEquals("00000000", systemA0.getId());
        assertEquals("System A0", systemA0.getName());
        assertEquals(true, systemA0.isStandard());
        assertEquals(1, systemA0.getServices().size());
        assertEquals("00000000", systemA0.getOrganization().getId());

        ServiceDefinition serviceA0_0 = systemA0.getServiceDefinition("00000000");
        assertNotNull(serviceA0_0);
        assertTrue(serviceA0_0.isValid());
        assertEquals("00000000", serviceA0_0.getId());
        assertEquals("Service A0-0", serviceA0_0.getName());
        assertEquals("M1", serviceA0_0.getRequestMessageTypeId());
        assertEquals("M2", serviceA0_0.getResponseMessageTypeId());
        assertNull(serviceA0_0.getServiceProviderBeanId());
        assertEquals(systemA0, serviceA0_0.getSystem());
    }

    @Test
    public void testReadFails() throws Exception
    {
        SystemDefinition system = dao.getSystemDefinition("00000000", "00000003");
        
        assertNull(system);
    }
}
