package org.egovframe.rte.psl.data.mongodb;

import com.mongodb.client.MongoClients;
import org.egovframe.rte.psl.data.mongodb.domain.Person;
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

import static org.junit.Assert.assertEquals;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/context-common.xml")
public class MongoOperationsAnonymousTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoOperationsAnonymousTest.class);

    @Value("${mongodb.host}")
    private String mongodbHost;

    @Value("${mongodb.port}")
    private int mongodbPort;

    @Before
    public void setUp() {
        LOGGER.info("##### MongoDB host : " + mongodbHost);
        LOGGER.info("##### MongoDB port : " + mongodbPort);
    }

    @Test
    public void testBasicOperations() {
        MongoOperations mongoOperations = new MongoTemplate(MongoClients.create("mongodb://"+mongodbHost+":"+mongodbPort), "user");

        Person person = new Person();
        person.setId("1001");
        person.setName("Kim");
        person.setAge(20);
        mongoOperations.save(person, "person");

        Person person1 = mongoOperations.findOne(new Query(where("name").is("Kim").and("age").is(20)), Person.class);
        LOGGER.info("##### MongoOperationsAnonymousTest person : " + person1);
        assertEquals(person.getId(), person1.getId());
        assertEquals(person.getName(), person1.getName());
        assertEquals(person.getAge(), person1.getAge());

        mongoOperations.dropCollection("person");
    }

}
