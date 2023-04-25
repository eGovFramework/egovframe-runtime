package org.egovframe.rte.ptl.mvc.handler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@Controller
public class InterceptorTestController {

	@RequestMapping("/test.do")
	public void test(HttpServletRequest request, HttpServletResponse response) {

		@SuppressWarnings("unchecked")
		ArrayList<String> array = (ArrayList<String>) request.getAttribute("interceptor");
		array.add("InterceptorTestController");
		request.setAttribute("interceptor", array);
	}
}