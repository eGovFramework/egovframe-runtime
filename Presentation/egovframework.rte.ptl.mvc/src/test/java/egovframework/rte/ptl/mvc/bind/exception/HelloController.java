package egovframework.rte.ptl.mvc.bind.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
	@RequestMapping("/form")
	public @ResponseBody String form() {
		throw new RuntimeException("It occurs RuntimeException!");
	}
}
