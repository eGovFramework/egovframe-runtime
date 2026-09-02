package org.egovframe.rte.ptl.reactive.exception;

import org.egovframe.rte.ptl.reactive.annotation.EgovController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class EgovExceptionHandlerTest {

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        this.webTestClient = WebTestClient.bindToController(new SampleController(), new KoreanMessageController())
                .controllerAdvice(new EgovExceptionHandler())
                .build();
    }

    @Test
    public void exceptionHandlerTest() {
        this.webTestClient.get()
                .uri("/test")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.parseMediaType("application/json;charset=UTF-8"));
    }

    @Test
    public void responseBodyIsParseableJson() {
        this.webTestClient.get()
                .uri("/test")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .consumeWith(result -> {
                    String body = new String(result.getResponseBody(), StandardCharsets.UTF_8);
                    JSONObject json;
                    try {
                        json = (JSONObject) new JSONParser().parse(body);
                    } catch (Exception e) {
                        throw new AssertionError("application/json 으로 선언한 본문이 파싱되지 않는다: " + body, e);
                    }
                    assertEquals(404L, json.get("status"));
                });
    }

    @Test
    public void koreanMessageTest() {
        this.webTestClient.get()
                .uri("/korean-message")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> assertTrue(new String(result.getResponseBody(), StandardCharsets.UTF_8)
                        .contains("\"message\":\"서비스 예외 메시지\"")));
    }

    @EgovController
    private static class KoreanMessageController {

        @GetMapping("/korean-message")
        public Mono<String> koreanMessage() {
            return Mono.error(new EgovServiceException(EgovErrorCode.INVALID_INPUT_VALUE, "서비스 예외 메시지"));
        }

    }

}
