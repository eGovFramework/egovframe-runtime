package org.egovframe.boot.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EgovLoggingPropertiesTest {

    @Test
    void baseLoggingIsEnabledByDefaultWithoutAnyConfiguration() {
        EgovLoggingProperties properties = new EgovLoggingProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getRequest().isEnabled()).isTrue();
        assertThat(properties.getRequest().getExcludePatterns()).isEqualTo("/actuator/**");
    }

    @Test
    void traceIsOptionalAndDisabledByDefault() {
        EgovLoggingProperties properties = new EgovLoggingProperties();

        assertThat(properties.getTrace().isEnabled()).isFalse();
        assertThat(properties.getTrace().getTraceIdKey()).isEqualTo("traceId");
        assertThat(properties.getTrace().getSpanIdKey()).isEqualTo("spanId");
    }

}
