package org.egovframe.boot.logging.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EgovTraceLoggingFilterTest {

    @Test
    void usesTracerSpanWhenAvailable() throws Exception {
        TraceContext context = mock(TraceContext.class);
        when(context.traceId()).thenReturn("abc123traceid");
        when(context.spanId()).thenReturn("def456spanid");

        Span span = mock(Span.class);
        when(span.context()).thenReturn(context);

        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(span);

        @SuppressWarnings("unchecked")
        ObjectProvider<Tracer> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(tracer);

        EgovTraceLoggingFilter filter = new EgovTraceLoggingFilter(provider, "traceId", "spanId");

        FilterChain chain = mock(FilterChain.class);
        String[] mdcTraceId = new String[1];
        String[] mdcSpanId = new String[1];
        doAnswer(invocation -> {
            mdcTraceId[0] = MDC.get("traceId");
            mdcSpanId[0] = MDC.get("spanId");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(mdcTraceId[0]).isEqualTo("abc123traceid");
        assertThat(mdcSpanId[0]).isEqualTo("def456spanid");
        verify(chain).doFilter(any(), any());
        assertThat(MDC.get("traceId")).isNull();
        assertThat(MDC.get("spanId")).isNull();
    }

    @Test
    void fallsBackToGeneratedTraceIdWhenNoTracerIsPresent() throws Exception {
        @SuppressWarnings("unchecked")
        ObjectProvider<Tracer> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        EgovTraceLoggingFilter filter = new EgovTraceLoggingFilter(provider, "traceId", "spanId");

        FilterChain chain = mock(FilterChain.class);
        String[] mdcTraceId = new String[1];
        doAnswer(invocation -> {
            mdcTraceId[0] = MDC.get("traceId");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(mdcTraceId[0]).isNotBlank();
        assertThat(MDC.get("spanId")).isNull();
        assertThat(MDC.get("traceId")).isNull();
    }

}
