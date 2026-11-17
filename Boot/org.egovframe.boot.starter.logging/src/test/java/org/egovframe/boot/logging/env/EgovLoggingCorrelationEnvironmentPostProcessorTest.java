package org.egovframe.boot.logging.env;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class EgovLoggingCorrelationEnvironmentPostProcessorTest {

    private final EgovLoggingCorrelationEnvironmentPostProcessor postProcessor =
            new EgovLoggingCorrelationEnvironmentPostProcessor();

    @Test
    void doesNotInjectByDefaultSinceTraceIsOptedOut() {
        MockEnvironment environment = new MockEnvironment();

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.containsProperty("logging.pattern.correlation")).isFalse();
    }

    @Test
    void injectsCorrelationPatternWhenTraceIsExplicitlyEnabled() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("org.egovframe.logging.trace.enabled", "true");

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("logging.pattern.correlation"))
                .isEqualTo("[%X{traceId:-},%X{spanId:-}] ");
    }

    @Test
    void doesNotOverrideUserDefinedPattern() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("org.egovframe.logging.trace.enabled", "true");
        environment.setProperty("logging.pattern.correlation", "custom-pattern");

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("logging.pattern.correlation")).isEqualTo("custom-pattern");
    }

    @Test
    void skipsInjectionWhenStarterDisabledEvenIfTraceEnabled() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("org.egovframe.logging.enabled", "false");
        environment.setProperty("org.egovframe.logging.trace.enabled", "true");

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.containsProperty("logging.pattern.correlation")).isFalse();
    }

    @Test
    void usesCustomMdcKeys() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("org.egovframe.logging.trace.enabled", "true");
        environment.setProperty("org.egovframe.logging.trace.trace-id-key", "myTraceId");
        environment.setProperty("org.egovframe.logging.trace.span-id-key", "mySpanId");

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("logging.pattern.correlation"))
                .isEqualTo("[%X{myTraceId:-},%X{mySpanId:-}] ");
    }

}
