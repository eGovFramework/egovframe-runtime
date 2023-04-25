package org.egovframe.rte.psl.data.mongodb;

import com.mongodb.BasicDBObject;
import org.egovframe.rte.psl.data.mongodb.domain.Address;
import org.egovframe.rte.psl.data.mongodb.domain.Person;
import org.egovframe.rte.psl.data.mongodb.repository.PersonRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/context-data-mongodb.xml")
public class PersonRepositoryTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonRepositoryTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PersonRepository personRepository;

    private Person makePerson() {
        Person person = new Person();
        person.setId("1001");
        person.setName("Jung");
        person.setAge(20);

        Address address = new Address();
        address.setZipCode("100-100");
        address.setAddress("Seoul, Korea");

        person.setAddress(address);
        person.setLocation(new double[]{43.0, 48.0});

        return person;
    }

    @PostConstruct
    public void before() {
        mongoTemplate.getCollection("person").createIndex(new BasicDBObject("location", "2d"));
    }

    @Before
    public void setUp() {
        Person person = makePerson();
        person = personRepository.save(person);
        LOGGER.info("##### PersonRepositoryTest Persion ID : " + person.getId());
    }

    @After
    public void tearDown() {
        personRepository.deleteAll();
    }

    @Test
    public void readsFirstPageCorrectly() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Person> persons = personRepository.findAll(pageable);
        LOGGER.info("##### PersonRepositoryTest Persons Total elements : " + persons.getTotalElements());
        assertTrue(persons.isFirst());
    }

    @Test
    public void testQueryMethods() {
        List<Person> list = personRepository.findByName("Jung");
        LOGGER.info("##### PersonRepositoryTest List : " + list.size());
        assertEquals(1, list.size());

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Person> persons = personRepository.findByName("Jung", pageable);

        assertTrue(persons.isFirst());
        assertEquals(1L, persons.getTotalElements());
        assertEquals(1, persons.getTotalPages());

        Person personList = list.get(0);
        LOGGER.info("##### PersonRepositoryTest personList : " + personList);

        Address address = personList.getAddress();
        Person personAddress = personRepository.findByAddress(address);
        LOGGER.info("##### PersonRepositoryTest Found personAddress : " + personAddress);

        assertEquals(personList, personAddress);
    }

    @Test
    public void testQueryAnnotation() {
        List<Person> list = personRepository.findByPersonName("Jung");
        assertEquals(1, list.size());
    }

    @Test
    public void testGeoSpatialMethods() {
        Point point = new Point(43.1, 48.1);
        Distance distance = new Distance(200, Metrics.KILOMETERS);
        List<Person> list = personRepository.findByLocationNear(point, distance);
        assertEquals(1, list.size());
    }

    @Test
    public void testDeleteMothods() {
        List<Person> list = personRepository.deleteByName("Jung");
        assertEquals(1, list.size());
    }

}
