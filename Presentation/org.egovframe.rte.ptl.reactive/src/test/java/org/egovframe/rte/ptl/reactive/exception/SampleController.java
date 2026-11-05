package org.egovframe.rte.ptl.reactive.exception;

import org.egovframe.rte.ptl.reactive.annotation.EgovController;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@EgovController
public class SampleController {

    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.error(new EgovException(EgovErrorCode.NOT_FOUND));
    }

}
