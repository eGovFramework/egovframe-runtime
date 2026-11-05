package org.egovframe.boot.logging.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EgovRequestLoggingFilterTest {

    private static final String ACCESS_LOGGER_NAME = "org.egovframe.boot.logging.access";

    private Logger accessLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        accessLogger = (Logger) LoggerFactory.getLogger(ACCESS_LOGGER_NAME);
        appender = new ListAppender<>();
        appender.start();
        accessLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        accessLogger.detachAppender(appender);
    }

    @Test
    void skipsExcludedPatterns() {
        EgovRequestLoggingFilter filter = new EgovRequestLoggingFilter("/actuator/**,/health");

        MockHttpServletRequest excluded = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletRequest included = new MockHttpServletRequest("GET", "/api/users");

        assertThat(filter.shouldNotFilter(excluded)).isTrue();
        assertThat(filter.shouldNotFilter(included)).isFalse();
    }

    @Test
    void invokesFilterChainForIncludedRequests() throws Exception {
        EgovRequestLoggingFilter filter = new EgovRequestLoggingFilter("");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(new MockHttpServletRequest("GET", "/api/users"), new MockHttpServletResponse(), chain);

        verify(chain, times(1)).doFilter(any(), any());
    }

    @Test
    void logsMethodUriStatusAndElapsedTime() throws Exception {
        EgovRequestLoggingFilter filter = new EgovRequestLoggingFilter("");
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(201);

        filter.doFilter(new MockHttpServletRequest("POST", "/api/users"), response, chain);

        assertThat(appender.list).hasSize(1);
        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message).startsWith("POST /api/users - status=201 time=");
    }

    @Test
    void sanitizesCrlfInRequestUriToPreventLogInjection() throws Exception {
        EgovRequestLoggingFilter filter = new EgovRequestLoggingFilter("");
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/api/users\r\n2026-08-25T00:00:00.000+09:00  INFO forged-log-entry");

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(appender.list).hasSize(1);
        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message).doesNotContain("\r").doesNotContain("\n");
        assertThat(message).contains("/api/users__2026-08-25T00:00:00.000+09:00  INFO forged-log-entry");
    }

}
