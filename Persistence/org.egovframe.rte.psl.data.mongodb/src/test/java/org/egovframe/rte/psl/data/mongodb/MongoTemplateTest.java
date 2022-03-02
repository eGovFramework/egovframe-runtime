package org.egovframe.rte.psl.data.mongodb;

import static org.junit.Assert.assertEquals;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import java.util.List;
import javax.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.egovframe.rte.psl.data.mongodb.domain.SimplePerson;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/context-data-*.xml")
public class MongoTemplateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoTemplateTest.class);

    @Resource(name = "mongoTemplate")
    private MongoTemplate mongoTemplate;

    @Test
    public void testBasicOperations() {
        SimplePerson person = new SimplePerson("Joe", 34);
        // Insert
        mongoTemplate.insert(person);
        LOGGER.info("Insert : " + person);

        // Find
        person = mongoTemplate.findOne(new Query(where("name").is("Joe")), SimplePerson.class);
        LOGGER.info("Found: " + person);

        assertEquals("Joe", person.getName());

        // Update
        mongoTemplate.updateFirst(query(where("name").is("Joe")), update("age", 35), SimplePerson.class);
        person = mongoTemplate.findOne(query(where("name").is("Joe")), SimplePerson.class);
        LOGGER.info("Updated: " + person);

        assertEquals(35, person.getAge());

        // Delete
        mongoTemplate.remove(person);

        // Check that deletion worked
        List<SimplePerson> people = mongoTemplate.findAll(SimplePerson.class);
        LOGGER.info("Number of people = : " + people.size());
        assertEquals(1, people.size());

        mongoTemplate.dropCollection("person");
    }

}
