package org.egovframe.rte.psl.reactive.redis.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=RedisConfiguration.class)
@Slf4j
public class RedisTest {

    @Autowired
    private SampleRepository repository;

    public Sample makeSample() {
        Sample sample = new Sample();
        sample.setId("1");
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
                    assertThat(item.booleanValue()).isEqualTo(true);
                    return true;
                })
                .verifyComplete();
    }

}
