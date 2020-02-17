package egovframework.rte.psl.data.mongodb;

import static org.junit.Assert.assertEquals;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.net.UnknownHostException;

import javax.annotation.Resource;

import egovframework.rte.psl.data.mongodb.domain.SimplePerson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.MongoClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/context-data-*.xml")
public class MongoOperationsAuthTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoOperationsAuthTest.class);
	
	@Resource(name="mongoTemplate")
	private  MongoTemplate mongoTemplate;
	
	@Before
	public void setUp() {
	
	}
	
	@Test
	public void testBasicOperations() throws UnknownHostException {

	    MongoOperations mongoOps = mongoTemplate;

	    mongoOps.insert(new SimplePerson("Joe", 34));
	    
	    SimplePerson person = mongoOps.findOne(new Query(where("name").is("Joe")), SimplePerson.class);
	    
	    assertEquals("Joe", person.getName());
	    

	    mongoOps.dropCollection("person");

	}
}
