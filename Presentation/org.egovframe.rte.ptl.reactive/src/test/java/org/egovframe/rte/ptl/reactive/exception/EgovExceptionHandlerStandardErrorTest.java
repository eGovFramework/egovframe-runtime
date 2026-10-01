package org.egovframe.rte.ptl.reactive.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovExceptionHandler — Spring 표준 예외 상태 보존·입력 검증 400·형 불일치 E002·MessageSource opt-in·
 * 커밋된 응답 불간섭 검증.
 *
 * <p>종전에는 모든 예외를 먼저 받는 처리기(@Order(-3))가 405·415·400·404 를 500 으로 바꿨다(처리기가 없으면
 * Spring 이 올바른 4xx 를 준다). 같은 요청을 처리기 유무 두 클라이언트에 보내 비교한다.</p>
 */
public class EgovExceptionHandlerStandardErrorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @RestController
    static class Api {
        @PostMapping("/items")
        public Mono<String> create(@Valid @RequestBody Item item) {
            return Mono.just("ok");
        }

        @GetMapping("/search")
        public Mono<String> search(@RequestParam("q") String q) {
            return Mono.just(q);
        }

        @GetMapping("/items/{id}")
        public Mono<String> find(@PathVariable("id") Long id) {
            return Mono.just(String.valueOf(id));
        }

        /** 클래스 수준 @Validated 없음 — 프레임워크 내장 메서드 파라미터 검증 경로 */
        @GetMapping("/sized")
        public Mono<String> sized(@RequestParam("size") @Min(1) @Max(100) int size) {
            return Mono.just(String.valueOf(size));
        }

        /** 요청 파라미터 이름이 자바 파라미터 이름과 다른 경우 — 필드 오류는 요청 쪽 이름을 쓴다 */
        @GetMapping("/paged")
        public Mono<String> paged(@RequestParam("page-size") @Min(1) int pageSize) {
            return Mono.just(String.valueOf(pageSize));
        }

        @GetMapping("/conflict")
        public Mono<String> conflict() {
            return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "duplicate secret-key"));
        }

        @GetMapping("/bad-gateway")
        public Mono<String> badGateway() {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "upstream secret-host"));
        }
    }

    public static class Item {
        @NotBlank
        public String name;
        @Size(max = 3)
        public String code;
    }

    private static WebTestClient client(EgovExceptionHandler handler) {
        WebTestClient.ControllerSpec spec = WebTestClient.bindToController(new Api());
        if (handler != null) {
            spec.controllerAdvice(handler);
        }
        return spec.build();
    }

    private static WebTestClient.ResponseSpec request(WebTestClient client, int i) {
        return switch (i) {
            case 0 -> client.get().uri("/items").exchange();
            case 1 -> client.post().uri("/items").contentType(MediaType.TEXT_PLAIN).bodyValue("x").exchange();
            case 2 -> client.post().uri("/items").contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{bad".getBytes(StandardCharsets.UTF_8)).exchange();
            case 3 -> client.get().uri("/search").exchange();
            default -> client.get().uri("/nope-secret-path").exchange();
        };
    }

    private static EntityExchangeResult<byte[]> result(WebTestClient.ResponseSpec spec) {
        return spec.expectBody().returnResult();
    }

    private static String text(EntityExchangeResult<byte[]> result) {
        byte[] body = result.getResponseBody();
        return (body == null) ? "" : new String(body, StandardCharsets.UTF_8);
    }

    private static JsonNode json(EntityExchangeResult<byte[]> result) throws Exception {
        return MAPPER.readTree(text(result));
    }

    @Test
    public void 표준_예외는_처리기가_있어도_Spring_기본과_같은_상태로_응답한다() throws Exception {
        WebTestClient plain = client(null);
        WebTestClient egov = client(new EgovExceptionHandler());
        int[] expected = {405, 415, 400, 400, 404};
        String[] codes = {"E008", "E012", "E001", "E001", "E007"};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result(request(plain, i)).getStatus().value(), "Spring 기본 상태 #" + i);
            EntityExchangeResult<byte[]> withHandler = result(request(egov, i));
            assertEquals(expected[i], withHandler.getStatus().value(), "처리기를 붙여도 같은 상태여야 한다 #" + i);
            JsonNode body = json(withHandler);
            assertEquals(expected[i], body.path("status").asInt(), "기본 형식 status #" + i);
            assertEquals(codes[i], body.path("code").asText(), "상태에 대응하는 오류 코드 #" + i);
            assertNotNull(body.get("timestamp"));
            assertFalse(body.path("message").asText().isEmpty());
        }
    }

    @Test
    public void 메서드_불허는_허용_메서드_헤더를_유지한다() throws Exception {
        EntityExchangeResult<byte[]> result = result(request(client(new EgovExceptionHandler()), 0));
        assertEquals(405, result.getStatus().value());
        String allow = result.getResponseHeaders().getFirst(HttpHeaders.ALLOW);
        assertNotNull(allow, "405 는 Allow 헤더를 가져야 한다");
        assertTrue(allow.contains("POST"), allow);
    }

    @Test
    public void 매핑_없는_경로와_깨진_본문은_입력과_파서_상세를_되비추지_않는다() {
        WebTestClient egov = client(new EgovExceptionHandler());
        String notFound = text(result(request(egov, 4)));
        assertFalse(notFound.contains("nope-secret-path"), notFound);
        String malformed = text(result(request(egov, 2)));
        assertFalse(malformed.contains("Unexpected character"), malformed);
        assertFalse(malformed.contains("Decoding"), malformed);
        assertFalse(malformed.contains("com.fasterxml"), malformed);
    }

    @Test
    public void problem_json_형식에도_표준_예외_상태와_코드가_담긴다() throws Exception {
        EgovExceptionHandler handler = new EgovExceptionHandler();
        handler.setProblemDetailEnabled(true);
        EntityExchangeResult<byte[]> result = result(request(client(handler), 0));
        assertEquals(405, result.getStatus().value());
        assertTrue(String.valueOf(result.getResponseHeaders().getContentType()).contains("application/problem+json"));
        assertNotNull(result.getResponseHeaders().getFirst(HttpHeaders.ALLOW));
        JsonNode body = json(result);
        assertEquals("Method Not Allowed", body.path("title").asText());
        assertEquals(405, body.path("status").asInt());
        assertEquals("E008", body.path("code").asText());
    }

    @Test
    public void 본문_크기_초과는_413과_E013() throws Exception {
        WebTestClient limited = WebTestClient.bindToController(new Api())
                .controllerAdvice(new EgovExceptionHandler())
                .httpMessageCodecs(codecs -> codecs.defaultCodecs().maxInMemorySize(64))
                .build();
        byte[] big = MAPPER.writeValueAsBytes(Map.of("name", "a".repeat(200)));
        EntityExchangeResult<byte[]> result = result(limited.post().uri("/items")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(big).exchange());
        assertEquals(413, result.getStatus().value());
        assertEquals("E013", json(result).path("code").asText());
    }

    @Test
    public void 대응_코드가_없는_상태는_계열_대표_코드이고_status_는_실제_값이다() throws Exception {
        WebTestClient egov = client(new EgovExceptionHandler());
        EntityExchangeResult<byte[]> conflict = result(egov.get().uri("/conflict").exchange());
        assertEquals(409, conflict.getStatus().value());
        JsonNode body = json(conflict);
        assertEquals(409, body.path("status").asInt());
        assertEquals("E001", body.path("code").asText());
        assertFalse(text(conflict).contains("secret-key"), "사유 문구도 일반화한다");
        EntityExchangeResult<byte[]> badGateway = result(egov.get().uri("/bad-gateway").exchange());
        assertEquals(502, badGateway.getStatus().value());
        assertEquals("E021", json(badGateway).path("code").asText());
        assertFalse(text(badGateway).contains("secret-host"), "5xx 사유 문구는 내부 정보일 수 있다");
    }

    @Test
    public void 본문_검증_실패는_400_E001과_필드_오류_목록이고_거부_값은_없다() throws Exception {
        byte[] invalid = MAPPER.writeValueAsBytes(Map.of("name", " ", "code", "TOOLONG-secret"));
        for (boolean problemDetail : new boolean[]{false, true}) {
            EgovExceptionHandler handler = new EgovExceptionHandler();
            handler.setProblemDetailEnabled(problemDetail);
            EntityExchangeResult<byte[]> result = result(client(handler).post().uri("/items")
                    .contentType(MediaType.APPLICATION_JSON).bodyValue(invalid).exchange());
            assertEquals(400, result.getStatus().value());
            JsonNode body = json(result);
            assertEquals("E001", body.path("code").asText());
            assertEquals(400, body.path("status").asInt());
            assertNotNull(body.get("timestamp"));
            assertNotNull(body.get(problemDetail ? "detail" : "message"), "기존 필드는 그대로다");
            if (problemDetail) {
                assertEquals("Validation Failed", body.path("title").asText(), "서블릿 MVC 와 같은 제목");
            }
            JsonNode errors = body.path("errors");
            assertEquals(2, errors.size(), errors.toString());
            Set<String> fields = new HashSet<>();
            errors.forEach(error -> fields.add(error.path("field").asText()));
            assertEquals(Set.of("name", "code"), fields);
            assertFalse(text(result).contains("TOOLONG-secret"), "거부된 값은 싣지 않는다");
        }
    }

    @Test
    public void 메서드_파라미터_검증_실패는_400과_요청_파라미터_이름의_필드_오류다() throws Exception {
        WebTestClient egov = client(new EgovExceptionHandler());
        EntityExchangeResult<byte[]> sized = result(egov.get().uri("/sized?size=500").exchange());
        assertEquals(400, sized.getStatus().value());
        JsonNode errors = json(sized).path("errors");
        assertEquals(1, errors.size(), errors.toString());
        assertEquals("size", errors.get(0).path("field").asText());
        assertFalse(errors.toString().contains("500"), "거부된 값은 싣지 않는다");
        EntityExchangeResult<byte[]> paged = result(egov.get().uri("/paged?page-size=0").exchange());
        assertEquals(400, paged.getStatus().value());
        assertEquals("page-size", json(paged).path("errors").get(0).path("field").asText());
    }

    @Test
    public void 형_불일치는_400_E002이고_입력과_타입명을_되비추지_않는다() throws Exception {
        EntityExchangeResult<byte[]> result = result(client(new EgovExceptionHandler()).get().uri("/items/abc-secret").exchange());
        assertEquals(400, result.getStatus().value());
        JsonNode body = json(result);
        assertEquals("E002", body.path("code").asText());
        assertEquals("id", body.path("errors").get(0).path("field").asText());
        assertEquals("Invalid value.", body.path("errors").get(0).path("message").asText());
        assertFalse(text(result).contains("abc-secret"), text(result));
        assertFalse(text(result).contains("java.lang"), text(result));
    }

    @Test
    public void MessageSource_연동_시_오류_코드_해석이_우선한다() throws Exception {
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("NotBlank.name", Locale.KOREAN, "이름은 필수입니다");
        messages.addMessage("typeMismatch.id", Locale.KOREAN, "아이디 형식이 올바르지 않습니다");
        EgovExceptionHandler handler = new EgovExceptionHandler();
        handler.setMessageSource(messages);
        WebTestClient egov = client(handler);
        byte[] invalid = MAPPER.writeValueAsBytes(Map.of("name", " ", "code", "ok"));
        JsonNode bodyErrors = json(result(egov.post().uri("/items").header(HttpHeaders.ACCEPT_LANGUAGE, "ko")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(invalid).exchange())).path("errors");
        assertEquals("이름은 필수입니다", bodyErrors.get(0).path("message").asText());
        JsonNode typeErrors = json(result(egov.get().uri("/items/abc").header(HttpHeaders.ACCEPT_LANGUAGE, "ko")
                .exchange())).path("errors");
        assertEquals("아이디 형식이 올바르지 않습니다", typeErrors.get(0).path("message").asText());
    }

    @Test
    public void 이미_커밋된_응답은_건드리지_않고_오류를_전파한다() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/stream").build());
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().setComplete().block();
        IllegalStateException original = new IllegalStateException("stream broke");
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> new EgovExceptionHandler().handle(exchange, original).block());
        assertSame(original, thrown, "처리기는 원래 오류를 그대로 흘려야 한다");
        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode(), "상태를 다시 쓰지 않는다");
    }

    @Test
    public void 오류_코드_415_413과_상태_지정_팩터리() {
        assertEquals(415, EgovErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus());
        assertEquals("E012", EgovErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode());
        assertEquals(413, EgovErrorCode.PAYLOAD_TOO_LARGE.getStatus());
        assertEquals("E013", EgovErrorCode.PAYLOAD_TOO_LARGE.getCode());
        EgovExceptionResponse response = EgovExceptionResponse.of(409, EgovErrorCode.INVALID_INPUT_VALUE, "m");
        assertEquals(409, response.getStatus());
        assertEquals("E001", response.getCode());
        assertEquals("m", response.getMessage());
        assertNotNull(response.getTimestamp());
        assertThrows(IllegalArgumentException.class, () -> EgovExceptionResponse.of(99, EgovErrorCode.INVALID_INPUT_VALUE, "m"));
        assertEquals(400, EgovExceptionResponse.of(EgovErrorCode.INVALID_INPUT_VALUE, "m").getStatus(), "기존 팩터리 유지");
    }
}
