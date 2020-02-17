package egovframework.rte.itl.integration.metadata.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import egovframework.rte.itl.integration.metadata.RecordTypeDefinition;

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
public class HibernateRecordTypeDefinitionDaoTest
{
    @Autowired
    private HibernateRecordTypeDefinitionDao dao;

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
    public void testReadFails() throws Exception
    {
        RecordTypeDefinition rtd = dao.getRecordTypeDefinition("x");
        assertNull(rtd);
    }
}
