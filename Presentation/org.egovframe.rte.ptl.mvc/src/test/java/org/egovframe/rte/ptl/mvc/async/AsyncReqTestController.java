package org.egovframe.rte.ptl.mvc.async;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AsyncReqTestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncReqTestController.class);

	@RequestMapping("/callable.do")
	public Callable<String> callableWithView(HttpServletRequest request, final Model model) {
		LOGGER.info("Before async processing");
		return new Callable<String>() {

			public String call() throws Exception {
				Thread.sleep(2000);
				LOGGER.info("Inside async processing");
				return "result";
			}
		};
	}
}
