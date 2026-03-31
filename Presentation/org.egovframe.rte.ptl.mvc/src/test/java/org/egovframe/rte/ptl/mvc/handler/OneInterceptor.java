package org.egovframe.rte.ptl.mvc.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

public class OneInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ArrayList<String> array = new ArrayList<String>();
        array.add("OneInterceptor.preHandle");
        request.setAttribute("interceptor", array);
        return true;
    }

    @SuppressWarnings("unchecked")
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        ArrayList<String> array = (ArrayList<String>) request.getAttribute("interceptor");
        array.add("OneInterceptor.postHandle");
        request.setAttribute("interceptor", array);
    }

}
