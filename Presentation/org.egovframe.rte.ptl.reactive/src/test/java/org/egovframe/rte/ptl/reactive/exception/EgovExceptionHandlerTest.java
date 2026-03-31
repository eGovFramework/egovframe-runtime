package org.egovframe.rte.ptl.reactive.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
public class EgovExceptionHandlerTest {

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        this.webTestClient = WebTestClient.bindToController(new SampleController())
                .controllerAdvice(new EgovExceptionHandler())
                .build();
    }

    @Test
    public void exceptionHandlerTest() {
        this.webTestClient.get()
                .uri("/test")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

}
