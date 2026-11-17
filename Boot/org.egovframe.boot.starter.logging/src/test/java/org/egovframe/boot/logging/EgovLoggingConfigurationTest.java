package org.egovframe.boot.logging;

import org.egovframe.boot.logging.filter.EgovRequestLoggingFilter;
import org.egovframe.boot.logging.filter.EgovTraceLoggingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

import static org.assertj.core.api.Assertions.assertThat;

class EgovLoggingConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EgovLoggingConfiguration.class));

    @Test
    void registersOnlyRequestLoggingFilterByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EgovLoggingProperties.class);
            assertThat(context).hasBean("egovRequestLoggingFilter");
            assertThat(context).doesNotHaveBean("egovTraceLoggingFilter");

            @SuppressWarnings("unchecked")
            FilterRegistrationBean<EgovRequestLoggingFilter> registration =
                    context.getBean("egovRequestLoggingFilter", FilterRegistrationBean.class);
            assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 1);
        });
    }

    @Test
    void registersTraceLoggingFilterWhenTraceEnabled() {
        contextRunner.withPropertyValues("org.egovframe.logging.trace.enabled=true")
                .run(context -> {
                    assertThat(context).hasBean("egovTraceLoggingFilter");

                    @SuppressWarnings("unchecked")
                    FilterRegistrationBean<EgovTraceLoggingFilter> registration =
                            context.getBean("egovTraceLoggingFilter", FilterRegistrationBean.class);
                    assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
                });
    }

    @Test
    void doesNotRegisterRequestLoggingFilterWhenDisabled() {
        contextRunner.withPropertyValues("org.egovframe.logging.request.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean("egovRequestLoggingFilter"));
    }

    @Test
    void registersNoBeansWhenStarterDisabled() {
        contextRunner.withPropertyValues("org.egovframe.logging.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(EgovLoggingProperties.class);
                    assertThat(context).doesNotHaveBean("egovRequestLoggingFilter");
                    assertThat(context).doesNotHaveBean("egovTraceLoggingFilter");
                });
    }

}
