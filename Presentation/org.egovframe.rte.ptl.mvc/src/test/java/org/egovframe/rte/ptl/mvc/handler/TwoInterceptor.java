package org.egovframe.rte.ptl.mvc.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

public class TwoInterceptor implements HandlerInterceptor {

    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ArrayList<String> array = (ArrayList<String>) request.getAttribute("interceptor");
        array.add("TwoInterceptor.preHandle");
        request.setAttribute("interceptor", array);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @SuppressWarnings("unchecked")
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        ArrayList<String> array = (ArrayList<String>) request.getAttribute("interceptor");
        array.add("TwoInterceptor.postHandle");
        request.setAttribute("interceptor", array);
    }

}
