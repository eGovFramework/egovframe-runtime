package org.egovframe.rte.ptl.reactive.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 오류 응답 본문의 형식을 검증한다 — 기본 형식(timestamp/status/code/message)과 RFC 9457 선택 형식.
 */
public class EgovExceptionHandlerResponseTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static MockServerWebExchange handle(EgovExceptionHandler handler, Throwable ex) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        handler.handle(exchange, ex).block();
        return exchange;
    }

    private static JsonNode body(MockServerWebExchange exchange) throws Exception {
        String body = exchange.getResponse().getBodyAsString().block();
        assertNotNull(body);
        return MAPPER.readTree(body);
    }

    private static List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            names.add(it.next());
        }
        return names;
    }

    @Test
    public void testDefaultFormatKeepsLegacyFieldsAndMediaType() throws Exception {
        MockServerWebExchange exchange = handle(new EgovExceptionHandler(), new EgovException("업무 오류가 발생했습니다"));

        assertEquals(MediaType.parseMediaType("application/json;charset=UTF-8"), exchange.getResponse().getHeaders().getContentType());
        JsonNode body = body(exchange);
        assertEquals(List.of("timestamp", "status", "code", "message"), fieldNames(body), "기존 형식의 필드가 그대로여야 한다");
        assertTrue(body.get("status").isInt(), "status 는 정수여야 한다: " + body);
        assertEquals("업무 오류가 발생했습니다", body.get("message").asText());
        assertNull(body.get("type"), "기본값에서는 Problem Details 필드가 없어야 한다");
    }

    @Test
    public void testUnhandledExceptionKeepsGenericMessage() throws Exception {
        MockServerWebExchange exchange = handle(new EgovExceptionHandler(), new IllegalStateException("jdbc:oracle secret detail"));

        JsonNode body = body(exchange);
        assertEquals(500, body.get("status").asInt());
        assertFalse(body.get("message").asText().contains("oracle"), "내부 정보가 응답에 노출되면 안 된다: " + body);
    }

    @Test
    public void testProblemDetailOptInUsesRfc9457ShapeAndMediaType() throws Exception {
        EgovExceptionHandler handler = new EgovExceptionHandler();
        handler.setProblemDetailEnabled(true);
        MockServerWebExchange exchange = handle(handler, new EgovServiceException(EgovErrorCode.INVALID_INPUT_VALUE, "검증에 실패했습니다"));

        MediaType contentType = exchange.getResponse().getHeaders().getContentType();
        assertNotNull(contentType);
        assertEquals("application", contentType.getType());
        assertEquals("problem+json", contentType.getSubtype());
        JsonNode body = body(exchange);
        assertEquals(List.of("type", "title", "status", "detail", "code", "timestamp"), fieldNames(body));
        assertEquals("about:blank", body.get("type").asText());
        assertEquals(400, body.get("status").asInt());
        assertEquals("Bad Request", body.get("title").asText());
        assertEquals("검증에 실패했습니다", body.get("detail").asText());
    }

    @Test
    public void testKoreanMessageIsWrittenAsUtf8() throws Exception {
        MockServerWebExchange exchange = handle(new EgovExceptionHandler(), new EgovServiceException(EgovErrorCode.INVALID_INPUT_VALUE, "서비스 예외 메시지"));

        String raw = exchange.getResponse().getBodyAsString().block();
        assertNotNull(raw);
        assertTrue(raw.contains("\"message\":\"서비스 예외 메시지\""), raw);
    }
}
