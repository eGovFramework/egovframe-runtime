package org.egovframe.rte.psl.data.mongodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mongodb.BasicDBObject;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
import org.egovframe.rte.psl.data.mongodb.domain.Address;
import org.egovframe.rte.psl.data.mongodb.domain.Person;
import org.egovframe.rte.psl.data.mongodb.repository.PersonRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/context-data-*.xml")
public class PersonRepositoryTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonRepositoryTest.class);

    @Autowired
    private PersonRepository repository;

    @Resource(name = "mongoTemplate")
    private MongoTemplate mongoTemplate;

    private Person makePerson() {
        Person person = new Person();
        person.setFirstname("Gildong");
        person.setLastname("Hong");

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
        person = repository.save(person);
        LOGGER.info("Persion ID : " + person.getId());
    }

    @After
    public void tearDown() {
        repository.deleteAll();
    }

    @Test
    public void readsFirstPageCorrectly() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Person> persons = repository.findAll(pageable);
        LOGGER.info("Persons Total elements : " + persons.getTotalElements());
        assertTrue(persons.isFirst());
    }

    @Test
    public void testQueryMethods() {
        List<Person> list = repository.findByLastname("Hong");
        LOGGER.info("Number of Hong = : " + list.size());
        assertEquals(1, list.size());

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Person> persons = repository.findByFirstname("Gildong", pageable);

        assertTrue(persons.isFirst());
        assertEquals(1L, persons.getTotalElements());
        assertEquals(1, persons.getTotalPages());

        Person hong = list.get(0);
        LOGGER.info("Hong Person id = " + hong.getId());

        Address address = hong.getAddress();
        Person found = repository.findByAddress(address);
        LOGGER.info("Found Person id = " + hong.getId());

        assertEquals(hong, found);
    }

    @Test
    public void testDeleteMothods() {
        Long deleted = repository.deletePersonByLastname("Hong");
        assertEquals(1L, deleted.longValue());
        List<Person> list = repository.deleteByLastname("Hong");
        assertEquals(0, list.size());
    }

    @Test
    public void testGeoSpatialMethods() {
        Point point = new Point(43.1, 48.1);
        Distance distance = new Distance(200, Metrics.KILOMETERS);
        List<Person> list = repository.findByLocationNear(point, distance);
        assertEquals(1, list.size());
    }

    @Test
    public void testQueryAnnotation() {
        List<Person> list = repository.findByThePersonsFirstname("Gildong");
        assertEquals(1, list.size());
    }
}
