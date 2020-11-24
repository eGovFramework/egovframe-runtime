package org.egovframe.rte.psl.data.mongodb;

import static org.junit.Assert.assertEquals;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.mongodb.MongoClient;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.egovframe.rte.psl.data.mongodb.domain.SimplePerson;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/context-common.xml")
public class MongoOperationsAnonymousTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoOperationsAnonymousTest.class);

    @Value("#{mongo['mongo.host']}")
    private String mongoHost;

    @Value("#{mongo['mongo.port']}")
    private int mongoPort;

    @Before
    public void setUp() {
        LOGGER.info("MongoDB host : " + mongoHost);
        LOGGER.info("MongoDB port : " + mongoPort);
    }

    @Test
    public void testBasicOperations() throws UnknownHostException {
        MongoOperations mongoOps = new MongoTemplate(new MongoClient(mongoHost, mongoPort), "database");
        mongoOps.insert(new SimplePerson("Joe", 34));
        SimplePerson person = mongoOps.findOne(new Query(where("name").is("Joe")), SimplePerson.class);
        assertEquals("Joe", person.getName());
        mongoOps.dropCollection("person");
    }
}
