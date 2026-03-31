package org.egovframe.rte.ptl.reactive.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class EgovNotNullValidationTest {

    private ValidatorFactory validatorFactory;

    private Validator validator;

    private Locale previousDefaultLocale;

    @BeforeEach
    public void setup() {
        previousDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.KOREAN);

        PlatformResourceBundleLocator resourceBundleLocator = new PlatformResourceBundleLocator("messages/message-validation");
        ResourceBundleMessageInterpolator messageInterpolator = new ResourceBundleMessageInterpolator(resourceBundleLocator);

        this.validatorFactory = Validation.byDefaultProvider().configure().messageInterpolator(messageInterpolator).buildValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    @AfterEach
    public void close() {
        try {
            this.validatorFactory.close();
        } finally {
            if (previousDefaultLocale != null) {
                Locale.setDefault(previousDefaultLocale);
            }
        }
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

        assertEquals("필수 입력값을 입력해주세요", validate.iterator().next().getMessage());
    }

}
