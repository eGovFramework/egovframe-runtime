package org.egovframe.rte.psl.reactive.mongodb.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=MongoDbConfiguration.class)
@Slf4j
public class MongoDbTest {

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

        repository.insertSample(sample)
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getId()).isNotNull();
                    assertThat(item.getSampleId()).isEqualTo("SAMPLE-00001");
                    assertThat(item.getName()).isEqualTo("Runtime");
                    assertThat(item.getDescription()).isEqualTo("Runtime Tool");
                    assertThat(item.getUseYn()).isEqualTo("Y");
                    assertThat(item.getRegUser()).isEqualTo("eGov");
                    return true;
                })
                .verifyComplete();
    }

}
