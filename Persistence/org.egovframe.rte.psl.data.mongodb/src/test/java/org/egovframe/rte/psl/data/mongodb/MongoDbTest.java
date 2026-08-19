package org.egovframe.rte.psl.data.mongodb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MongoDbConfiguration.class)
public class MongoDbTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbTest.class);

    @Autowired
    private SampleRepository repository;

    public Sample makeSample() {
        Sample sample = new Sample();
        sample.setId(1);
        sample.setSampleId("SAMPLE-00001");
        sample.setName("Runtime");
        sample.setDescription("Runtime Tool");
        sample.setUseYn("Y");
        sample.setRegUser("eGov");
        return sample;
    }

    @Test
    public void insertDataRejectsDuplicateId() {
        Sample sample = makeSample();

        repository.deleteSample(sample);
        repository.insertSample(sample);

        Sample duplicate = makeSample();
        duplicate.setName("Overwritten");

        assertThrows(DuplicateKeyException.class, () -> repository.insertSample(duplicate));
        assertEquals("Runtime", repository.selectOneSample(1).getName());
    }

    @Test
    public void crudTest() {
        Sample sample = makeSample();

        repository.deleteSample(sample);

        repository.insertSample(sample);

        Sample newValue = repository.selectOneSample(1);
        LOGGER.debug("### MongoDbTest crudTest() id : {}", newValue.getId());
        LOGGER.debug("### MongoDbTest crudTest() sample id : {}", newValue.getSampleId());
        LOGGER.debug("### MongoDbTest crudTest() name : {}", newValue.getName());
        LOGGER.debug("### MongoDbTest crudTest() description : {}", newValue.getDescription());
        LOGGER.debug("### MongoDbTest crudTest() useYn : {}", newValue.getUseYn());
        LOGGER.debug("### MongoDbTest crudTest() regUser : {}", newValue.getRegUser());

        assertNotNull(newValue.getId());
        assertEquals("SAMPLE-00001", newValue.getSampleId());
        assertEquals("Runtime", newValue.getName());
        assertEquals("Runtime Tool", newValue.getDescription());
        assertEquals("Y", newValue.getUseYn());
        assertEquals("eGov", newValue.getRegUser());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void deprecatedDeleteDateStillDeletes() {
        Sample sample = makeSample();

        repository.deleteSample(sample);
        repository.insertSample(sample);

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(sample.getId()));
        repository.deleteDate(query, Sample.class);

        assertNull(repository.selectOneSample(sample.getId()));
    }

}
