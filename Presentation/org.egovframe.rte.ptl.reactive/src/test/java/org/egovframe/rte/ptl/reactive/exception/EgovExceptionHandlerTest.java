package org.egovframe.rte.ptl.reactive.exception;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

public class EgovExceptionHandlerTest {

    private WebTestClient webTestClient;

    @Before
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
