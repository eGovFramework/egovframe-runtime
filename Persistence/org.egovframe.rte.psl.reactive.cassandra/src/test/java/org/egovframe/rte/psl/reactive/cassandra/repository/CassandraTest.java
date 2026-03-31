package org.egovframe.rte.psl.reactive.cassandra.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CassandraConfiguration.class)
@Slf4j
public class CassandraTest {

    @Autowired
    private SampleRepository repository;

    public Sample makeSample() {
        Sample sample = new Sample();
        sample.setId("part01");
        sample.setSampleId("SAMPLE-00001");
        sample.setName("Runtime");
        sample.setDescription("Runtime Tool");
        sample.setUseYn("Y");
        sample.setRegUser("eGov");
        return sample;
    }

    @Test
    public void crudTest() {
        Sample sample = makeSample();

        repository.deleteSample(sample).block();

        repository.insertSample(sample)
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    log.debug("item id >>> {}", item.getId());
                    log.debug("item sample id >>> {}", item.getSampleId());
                    log.debug("item name >>> {}", item.getName());
                    log.debug("item description >>> {}", item.getDescription());
                    log.debug("item useYn >>> {}", item.getUseYn());
                    log.debug("item regUser >>> {}", item.getRegUser());

                    assertNotNull(item.getId());
                    assertEquals("SAMPLE-00001", item.getSampleId());
                    assertEquals("Runtime", item.getName());
                    assertEquals("Runtime Tool", item.getDescription());
                    assertEquals("Y", item.getUseYn());
                    assertEquals("eGov", item.getRegUser());
                    return true;
                })
                .verifyComplete();
    }

}
