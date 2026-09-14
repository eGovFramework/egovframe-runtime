package org.egovframe.rte.fdl.reactive.logging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovReactiveWebAccessLogFilter 의 접근로그 1줄 형식, 오류·5xx 처리, 제외 패턴, 속성 폴백, 기록 실패 격리를 검증한다.
 */
public class EgovReactiveWebAccessLogFilterTest {

    static class CapturingFilter extends EgovReactiveWebAccessLogFilter {
        final List<String> lines = new ArrayList<>();
        final List<Boolean> failed = new ArrayList<>();

        @Override
        protected void emit(boolean isFailed, String line) {
            failed.add(isFailed);
            lines.add(line);
        }
    }

    private static final WebFilterChain OK = exchange -> {
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        return Mono.empty();
    };

    @Test
    public void testNormalRequestIsRecordedAsOneLineWithAttributes() {
        CapturingFilter filter = new CapturingFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/list"));
        exchange.getAttributes().put(EgovReactiveWebAccessLogFilter.ATTR_REQUEST_ID, "req-1");
        exchange.getAttributes().put(EgovReactiveWebAccessLogFilter.ATTR_CLIENT_IP, "10.0.0.9");
        exchange.getAttributes().put(EgovReactiveWebAccessLogFilter.ATTR_USER_ID, "hong");

        filter.filter(exchange, OK).block();

        assertEquals(1, filter.lines.size());
        String line = filter.lines.get(0);
        assertTrue(line.startsWith("method=GET uri=/api/list status=200 elapsedMs="), line);
        assertTrue(line.endsWith(" requestId=req-1 clientIp=10.0.0.9 userId=hong"), line);
        assertEquals(false, filter.failed.get(0));
    }

    @Test
    public void testFailedRequestIsRecordedWithStatus500AndErrorThenPropagated() {
        CapturingFilter filter = new CapturingFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/fail"));

        assertThrows(IllegalStateException.class,
                () -> filter.filter(exchange, ex -> Mono.error(new IllegalStateException("boom"))).block());

        assertEquals(1, filter.lines.size());
        String line = filter.lines.get(0);
        assertTrue(line.contains("uri=/api/fail status=500"), line);
        assertTrue(line.endsWith(" error=IllegalStateException"), line);
        assertEquals(true, filter.failed.get(0));
    }

    @Test
    public void testServerErrorStatusIsMarkedFailed() {
        CapturingFilter filter = new CapturingFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/busy"));

        filter.filter(exchange, ex -> {
            ex.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return Mono.empty();
        }).block();

        assertTrue(filter.lines.get(0).startsWith("method=POST uri=/api/busy status=503 "), filter.lines.get(0));
        assertFalse(filter.lines.get(0).contains(" error="), filter.lines.get(0));
        assertEquals(true, filter.failed.get(0));
    }

    @Test
    public void testQueryStringIsOmittedByDefaultAndExcludedPathsAreNotRecorded() {
        CapturingFilter filter = new CapturingFilter();
        filter.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/api/search?rrn=900101-1234567")), OK).block();
        assertFalse(filter.lines.get(0).contains("rrn="), filter.lines.get(0));

        CapturingFilter verbose = new CapturingFilter();
        verbose.setIncludeQueryString(true);
        verbose.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/api/search?rrn=1")), OK).block();
        assertTrue(verbose.lines.get(0).contains(" uri=/api/search?rrn=1 "), verbose.lines.get(0));

        CapturingFilter excluded = new CapturingFilter();
        excluded.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/css/site.css")), OK).block();
        excluded.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/actuator/health")), OK).block();
        assertTrue(excluded.lines.isEmpty(), "excluded: " + excluded.lines);

        CapturingFilter none = new CapturingFilter();
        none.setExcludePathPatterns(null);
        none.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/css/site.css")), OK).block();
        assertEquals(1, none.lines.size());
    }

    @Test
    public void testFallsBackToRequestIdAndRemoteAddressWhenAttributesAreAbsent() {
        CapturingFilter filter = new CapturingFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/secure")
                .remoteAddress(new InetSocketAddress("192.168.1.7", 45678)));

        filter.filter(exchange, OK).block();

        String line = filter.lines.get(0);
        assertTrue(line.endsWith(" requestId=" + exchange.getRequest().getId() + " clientIp=192.168.1.7 userId=-"), line);
    }

    @Test
    public void testRecordingFailureDoesNotAffectTheRequest() {
        EgovReactiveWebAccessLogFilter broken = new EgovReactiveWebAccessLogFilter() {
            @Override
            protected void emit(boolean failed, String line) {
                throw new IllegalStateException("appender down");
            }
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/ok"));

        broken.filter(exchange, OK).block();

        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
    }

}
