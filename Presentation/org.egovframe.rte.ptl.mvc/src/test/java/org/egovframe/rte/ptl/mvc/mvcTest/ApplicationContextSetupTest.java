package org.egovframe.rte.ptl.mvc.mvcTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
public class ApplicationContextSetupTest {

    @Test
    public void responseBodyHandler() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController()).build();
        mockMvc.perform(get("/form")).andExpect(status().isOk()).andExpect(content().string("hello"));
        mockMvc.perform(get("/wrong")).andExpect(status().isNotFound());
    }

    @Controller
    static class TestController {
        @RequestMapping("/form")
        public @ResponseBody String form() {
            return "hello";
        }
    }

}
