package org.egovframe.rte.ptl.mvc.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.Callable;

@Controller
public class AsyncReqTestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncReqTestController.class);

    @RequestMapping("/callable")
    public Callable<String> callableWithView() {
        LOGGER.debug("### AsyncReqTestController callableWithView() Before async processing");
        return () -> {
            Thread.sleep(2000);
            LOGGER.debug("### AsyncReqTestController callableWithView() Inside async processing");
            return "result";
        };
    }

}
