package org.egovframe.rte.psl.data.mongodb;

import org.egovframe.rte.psl.data.mongodb.domain.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
        mongoTemplate.dropCollection("person");
        mongoTemplate.createCollection("person");

        person.setId("1001");
        person.setName("Kim");
        person.setAge(20);

        // Insert
        mongoTemplate.save(person, "person");
        LOGGER.info("##### MongoTemplateTest person Insert : " + person);

        // Find
        person = mongoTemplate.findOne(query(where("_id").is("1001")), Person.class);
        LOGGER.info("##### MongoTemplateTest person Find : " + person);

        // Update
        mongoTemplate.upsert(query(where("_id").is("1001")), update("age", 40), Person.class);
        person = mongoTemplate.findById("1001", Person.class);
        LOGGER.info("##### MongoTemplateTest person Update : " + person);

        // Delete
        mongoTemplate.remove(person);
        LOGGER.info("##### MongoTemplateTest person Delete : " + person);
    }

}
