package org.egovframe.rte.ptl.mvc.bind.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * EgovRestExceptionHandler — Spring 표준 예외 상태 보존·메서드 파라미터 검증·인증·인가 예외 위임 검증.
 *
 * <p>종전에는 모든 예외를 받는 처리기가 405·415·404·필수 파라미터 누락까지 500 으로 바꿨다(처리기가 없으면
 * Spring 이 올바른 4xx 를 준다). 같은 요청을 처리기 유무 두 MockMvc 에 보내 비교한다.</p>
 */
public class EgovRestExceptionHandlerStandardErrorTest {

    @RestController
    static class Api {
        @PostMapping("/items")
        public String create(@RequestBody Item item) {
            return "ok";
        }

        @GetMapping("/search")
        public String search(@RequestParam("q") String q) {
            return q;
        }

        @GetMapping("/items/{id}")
        public String find(@PathVariable("id") Long id) {
            return String.valueOf(id);
        }

        /** 클래스 수준 @Validated 없음 — 프레임워크 내장 메서드 파라미터 검증 경로 */
        @GetMapping("/sized")
        public String sized(@RequestParam("size") @Min(1) @Max(100) int size) {
            return String.valueOf(size);
        }

        /** 요청 파라미터 이름이 자바 파라미터 이름과 다른 경우 — 필드 오류는 요청 쪽 이름을 쓴다 */
        @GetMapping("/paged")
        public String paged(@RequestParam("page-size") @Min(1) int pageSize) {
            return String.valueOf(pageSize);
        }

        @GetMapping("/denied")
        public String denied() {
            throw new org.springframework.security.access.AccessDeniedException("denied for test");
        }

        @GetMapping("/lookalike")
        public String lookalike() {
            throw new Fake.AccessDeniedException("same simple name, different package");
        }

        @GetMapping("/boom")
        public String boom() {
            throw new IllegalStateException("secret internal detail");
        }
    }

    public static class Item {
        public String name;
    }

    /** 이름만 같은 예외 — 전체 이름으로 판별하므로 보안 계층에 넘기지 않고 내부 오류로 일반화돼야 한다. */
    static class Fake {
        static class AccessDeniedException extends RuntimeException {
            AccessDeniedException(String message) {
                super(message);
            }
        }
    }

    @RestControllerAdvice
    static class Advice extends EgovRestExceptionHandler {
    }

    private final MockMvc plain = MockMvcBuilders.standaloneSetup(new Api()).build();
    private final MockMvc egov = MockMvcBuilders.standaloneSetup(new Api()).setControllerAdvice(new Advice()).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static MockHttpServletResponse perform(MockMvc mvc, RequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse();
    }

    private JsonNode body(MockHttpServletResponse response) throws Exception {
        return objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    /**
     * instance 는 Spring 이 비어 있을 때 요청 경로(쿼리 제외)로 채우는 RFC 9457 표준 멤버라 제외하고 본다
     * (종전의 모든 응답과 공식 main 이 같다 — design D1, 적용 중 발견).
     */
    private String bodyWithoutInstance(MockHttpServletResponse response) throws Exception {
        ObjectNode node = (ObjectNode) body(response);
        node.remove("instance");
        return node.toString();
    }

    @Test
    public void 표준_예외는_처리기가_있어도_Spring_기본과_같은_상태로_응답한다() throws Exception {
        RequestBuilder[] requests = {
                get("/items"),                                                            // 405
                post("/items").contentType(MediaType.TEXT_PLAIN).content("x"),            // 415
                post("/items").contentType(MediaType.APPLICATION_JSON).content("{bad"),   // 400 깨진 JSON
                get("/search"),                                                           // 400 필수 파라미터 누락
                get("/nope")                                                              // 404
        };
        int[] expected = {405, 415, 400, 400, 404};
        for (int i = 0; i < requests.length; i++) {
            MockHttpServletResponse withoutHandler = perform(plain, requests[i]);
            MockHttpServletResponse withHandler = perform(egov, requests[i]);
            assertEquals(expected[i], withoutHandler.getStatus(), "Spring 기본 상태 #" + i);
            assertEquals(expected[i], withHandler.getStatus(), "처리기를 붙여도 같은 상태여야 한다 #" + i);
            assertEquals("application/problem+json", withHandler.getContentType(), "problem+json #" + i);
            JsonNode node = body(withHandler);
            assertEquals(expected[i], node.path("status").asInt());
            assertNotNull(node.path("timestamp").textValue(), "timestamp 확장 필드 #" + i);
        }
    }

    @Test
    public void 메서드_불허는_허용_메서드_헤더를_유지한다() throws Exception {
        MockHttpServletResponse response = perform(egov, get("/items"));
        assertEquals(405, response.getStatus());
        String allow = response.getHeader("Allow");
        assertNotNull(allow, "405 는 Allow 헤더를 가져야 한다");
        assertTrue(allow.contains("POST"), allow);
        assertEquals("The request method is not supported for this resource.", body(response).path("detail").asText());
    }

    @Test
    public void 매핑_없는_경로는_404이고_설명에_요청_경로를_되비추지_않는다() throws Exception {
        MockHttpServletResponse response = perform(egov, get("/nope-secret-path"));
        assertEquals(404, response.getStatus());
        String content = bodyWithoutInstance(response);
        assertFalse(content.contains("nope-secret-path"), content);
        assertEquals("The requested resource was not found.", body(response).path("detail").asText());
        assertEquals("/nope-secret-path", body(response).path("instance").asText(), "instance 는 Spring 표준 동작(종전과 같음)");
    }

    @Test
    public void 필수_파라미터_누락은_400이고_일반화_문구다() throws Exception {
        MockHttpServletResponse response = perform(egov, get("/search"));
        assertEquals(400, response.getStatus());
        assertEquals("The request is invalid.", body(response).path("detail").asText());
    }

    @Test
    public void 경로_변수_형_불일치는_400이고_설명과_오류_목록에_입력을_되비추지_않는다() throws Exception {
        MockHttpServletResponse response = perform(egov, get("/items/abc-secret"));
        assertEquals(400, response.getStatus());
        String content = bodyWithoutInstance(response);
        assertFalse(content.contains("abc-secret"), content);
        assertFalse(content.contains("java.lang.Long"), content);
        assertEquals("id", body(response).path("errors").get(0).path("field").asText());
    }

    @Test
    public void 요청_파라미터_형_불일치는_응답_어디에도_입력을_되비추지_않는다() throws Exception {
        MockHttpServletResponse response = perform(egov, get("/sized").param("size", "zz-secret"));
        assertEquals(400, response.getStatus());
        String content = response.getContentAsString(StandardCharsets.UTF_8);
        assertFalse(content.contains("zz-secret"), content);
        assertFalse(content.contains("java.lang"), content);
        assertEquals("size", body(response).path("errors").get(0).path("field").asText());
    }

    @Test
    public void 메서드_파라미터_검증_실패는_제약_위반과_같은_400_필드_오류다() throws Exception {
        MockHttpServletResponse response = perform(egov, get("/sized").param("size", "500"));
        assertEquals(400, response.getStatus());
        JsonNode node = body(response);
        assertEquals("Validation Failed", node.path("title").asText());
        JsonNode errors = node.path("errors");
        assertEquals(1, errors.size(), errors.toString());
        assertEquals("size", errors.get(0).path("field").asText());
        assertFalse(errors.get(0).path("message").asText().isEmpty());
        assertFalse(response.getContentAsString(StandardCharsets.UTF_8).contains("500"), "거부된 값은 싣지 않는다");
    }

    @Test
    public void 메서드_검증_필드_이름은_요청_파라미터_이름이다() throws Exception {
        MockHttpServletResponse response = perform(egov, get("/paged").param("page-size", "0"));
        assertEquals(400, response.getStatus());
        assertEquals("page-size", body(response).path("errors").get(0).path("field").asText());
    }

    @Test
    public void 인가_거부는_500이_되지_않고_보안_계층으로_전파된다() {
        Exception thrown = assertThrows(Exception.class, () -> perform(egov, get("/denied")));
        Throwable cause = thrown;
        while (cause != null && !(cause instanceof org.springframework.security.access.AccessDeniedException)) {
            cause = cause.getCause();
        }
        assertNotNull(cause, "원래의 인가 거부 예외가 전파돼야 한다: " + thrown);
    }

    @Test
    public void 이름만_같은_예외는_보안_위임_대상이_아니고_내부_오류로_일반화된다() throws Exception {
        MockHttpServletResponse response = perform(egov, get("/lookalike"));
        assertEquals(500, response.getStatus());
        assertFalse(response.getContentAsString(StandardCharsets.UTF_8).contains("same simple name"));
    }

    @Test
    public void 미분류_예외는_여전히_500과_일반화_메시지다() throws Exception {
        MockHttpServletResponse response = perform(egov, get("/boom"));
        assertEquals(500, response.getStatus());
        String content = response.getContentAsString(StandardCharsets.UTF_8);
        assertFalse(content.contains("secret internal detail"), content);
        assertEquals("An internal server error has occurred.", body(response).path("detail").asText());
    }
}
