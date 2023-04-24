package org.egovframe.rte.psl.data.mongodb;

import org.egovframe.rte.psl.data.mongodb.domain.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/context-data-mongodb.xml")
public class MongoTemplateTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoTemplateTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testBasicOperations() {
        Person person = new Person();
        person.setId("1001");
        person.setName("Kim");
        person.setAge(20);

        // Insert
        mongoTemplate.save(person, "person");
        LOGGER.info("##### MongoTemplateTest person Insert : " + person);

        // Find
        person = mongoTemplate.findOne(new Query(where("id").is("1001")), Person.class);
        LOGGER.info("##### MongoTemplateTest person Find : " + person);

        // Update
        mongoTemplate.updateFirst(query(where("id").is("1001")), update("age", 30), Person.class);
        person = mongoTemplate.findOne(query(where("id").is("1001")), Person.class);
        LOGGER.info("##### MongoTemplateTest person Update : " + person);

        // Delete
        mongoTemplate.remove(person);

        // Find
        List<Person> people = mongoTemplate.findAll(Person.class);
        LOGGER.info("##### MongoTemplateTest person size : " + people.size());
        assertEquals(0, people.size());
    }

}
