package org.egovframe.rte.ptl.mvc.async;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/async/test_servlet.xml")
public class AsyncReqTest {

	@Test
	public void responseBodyHandler() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AsyncReqTestController()).build();
		mockMvc.perform(get("/callable.do")).andExpect(request().asyncStarted()).andExpect(request().asyncResult("result"));
	}

}
