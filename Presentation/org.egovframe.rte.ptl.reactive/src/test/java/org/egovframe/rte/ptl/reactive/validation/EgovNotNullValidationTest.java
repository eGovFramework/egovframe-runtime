package org.egovframe.rte.ptl.reactive.validation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class EgovNotNullValidationTest {

    private ValidatorFactory validatorFactory;

    private Validator validator;

    @Before
    public void setup() {
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    @After
    public void close() {
        this.validatorFactory.close();
    }

    public SampleDto makeSample() {
        SampleDto sampleDto = new SampleDto();
        sampleDto.setId(1);
        sampleDto.setSampleId(null);
        sampleDto.setName("Runtime");
        sampleDto.setDescription("Runtime Tool");
        sampleDto.setUseYn("Y");
        sampleDto.setRegUser("eGov");
        return sampleDto;
    }

    @Test
    public void validateTest() {
        SampleDto sampleDto = makeSample();

        Set<ConstraintViolation<SampleDto>> validate = this.validator.validate(sampleDto);

        assertEquals("Please enter required values", validate.iterator().next().getMessage());
    }

}
