package org.egovframe.rte.ptl.reactive.exception;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * WebFlux 예외 응답이 내부 정보·요청 경로를 노출하지 않는지 검증한다(CWE-209).
 */
public class EgovExceptionHandlerSecurityTest {

    private final EgovExceptionHandler handler = new EgovExceptionHandler();

    @Test
    public void 미분류_예외는_일반화_메시지로_500() {
        MockServerWebExchange exchange = exchange("/api/list");
        handler.handle(exchange, new IllegalStateException("jdbc:oracle:thin:@10.0.0.5:1521/SECRET")).block();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        String body = exchange.getResponse().getBodyAsString().block();
        assertFalse(body, body.contains("SECRET"));
    }

    @Test
    public void 표준_상태_예외는_상태를_보존하고_경로를_되비추지_않는다() {
        MockServerWebExchange exchange = exchange("/x");
        handler.handle(exchange, new ResponseStatusException(HttpStatus.NOT_FOUND, "No static resource /<script>x</script>")).block();
        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
        String body = exchange.getResponse().getBodyAsString().block();
        assertFalse(body, body.contains("<script>"));
    }

    private static MockServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path));
    }

}
