package egovframework.rte.itl.integration.metadata.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;

import egovframework.rte.itl.integration.metadata.IntegrationDefinition;

import javax.sql.DataSource;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/egovframework/rte/itl/integration/metadata/dao/hibernate/context.xml")
@TransactionConfiguration(defaultRollback=true)
@Transactional(readOnly=false)
public class HibernateIntegrationDefinitionDaoTest
{

    @Autowired
    private HibernateIntegrationDefinitionDao dao;

    @Autowired
    private DataSource dataSource;

    @SuppressWarnings("deprecation")
	@Before
    public void before() throws Exception
    {
        ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
                ResourceUtils.getFile("classpath:egovframework/rte/itl/integration/metadata/dao/hibernate/dataset.xml")));
        dataSet.addReplacementObject("[null]", null);
        
        IDatabaseConnection connection = new SpringDatabaseDataSourceConnection(dataSource);
        
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    @Test
    public void testReadSucceeds() throws Exception
    {
        IntegrationDefinition integrationDefinition = dao.getIntegrationDefinition("1");
        assertNotNull(integrationDefinition);
        assertTrue(integrationDefinition.isValid());
        assertEquals("1", integrationDefinition.getProvider().getKey());
        assertEquals("1", integrationDefinition.getConsumer().getKey());
        assertEquals(true, integrationDefinition.isUsing());
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
    public void testReadOfConsumerSucceeds() throws Exception
    {
        List<IntegrationDefinition> list = dao.getIntegrationDefinitionOfConsumer("00000000", "00000000");
        assertEquals(2, list.size());
        assertEquals("1", list.get(0).getId());
        assertTrue(list.get(0).isValid());
        assertEquals("2", list.get(1).getId());
        assertTrue(list.get(1).isValid());
    }
    
    @Test
    public void testReadOfProviderSucceeds() throws Exception
    {
        List<IntegrationDefinition> list = dao.getIntegrationDefinitionOfProvider("00000000", "00000000");
        assertEquals(1, list.size());
        assertEquals("1", list.get(0).getId());
        assertTrue(list.get(0).isValid());
    }
    
    @Test
    public void testReadFails() throws Exception
    {
        IntegrationDefinition integrationRegistry = dao.getIntegrationDefinition("0");
        
        assertNull(integrationRegistry);
    }
}
