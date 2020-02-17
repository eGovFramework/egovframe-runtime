package egovframework.rte.ptl.mvc.mvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/mvcTest/test_servlet.xml")
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
