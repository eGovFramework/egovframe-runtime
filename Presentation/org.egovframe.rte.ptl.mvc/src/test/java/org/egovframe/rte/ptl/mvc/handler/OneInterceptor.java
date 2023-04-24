package org.egovframe.rte.ptl.mvc.handler;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

public class OneInterceptor extends HandlerInterceptorAdapter {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		ArrayList<String> array = new ArrayList<String>();
		array.add("OneInterceptor.preHandle");
		request.setAttribute("interceptor", array);
		return super.preHandle(request, response, handler);
	}

	@SuppressWarnings("unchecked")
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

		ArrayList<String> array = (ArrayList<String>) request.getAttribute("interceptor");
		array.add("OneInterceptor.postHandle");
		request.setAttribute("interceptor", array);
		super.postHandle(request, response, handler, modelAndView);
	}
}