package org.egovframe.rte.ptl.mvc.async;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = "org.egovframe.rte.ptl.mvc.async")
public class AsyncReqTest {

    @Test
    public void responseBodyHandler() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AsyncReqTestController()).build();

        mockMvc.perform(get("/callable"))
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult("result"));
    }

}
