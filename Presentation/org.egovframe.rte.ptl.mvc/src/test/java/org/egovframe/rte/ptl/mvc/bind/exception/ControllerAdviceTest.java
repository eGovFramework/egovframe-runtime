package org.egovframe.rte.ptl.mvc.bind.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = "org.egovframe.rte.ptl.mvc.bind.exception")
public class ControllerAdviceTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(HelloController.class)
                .setControllerAdvice(AnnotationExceptionHandler.class)
                .build();
    }

    @Test
    public void exceptionHandlerTest() throws Exception {
        mockMvc.perform(get("/form"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("exceptionMsg", "RuntimeException.class"));

        mockMvc.perform(get("/wrong"))
                .andExpect(status().isNotFound());
    }

}
