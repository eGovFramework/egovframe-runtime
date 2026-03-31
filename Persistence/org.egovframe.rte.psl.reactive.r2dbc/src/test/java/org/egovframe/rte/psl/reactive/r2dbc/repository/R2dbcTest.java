package org.egovframe.rte.psl.reactive.r2dbc.repository;

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
@ContextConfiguration(classes = R2dbcConfiguration.class)
@Slf4j
public class R2dbcTest {

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
    public void crudTest() {
        Sample sample = makeSample();

        repository.deleteSample(sample);

        repository.insertSample(sample)
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
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
