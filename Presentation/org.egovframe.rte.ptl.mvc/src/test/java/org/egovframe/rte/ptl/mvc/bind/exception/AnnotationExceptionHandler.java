package org.egovframe.rte.ptl.mvc.bind.exception;

import org.egovframe.rte.fdl.cmmn.exception.BaseException;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@ControllerAdvice
public class AnnotationExceptionHandler extends AbstractAnnotationExceptionHandler{

	@Override
	public ModelAndView handleException(Exception e) {
		System.out.println("Exception발생");
		e.printStackTrace();

		ModelAndView model = new ModelAndView();
		model.addObject("exceptionMsg", "Exception.class");
		return model;
	}

	@Override
	public ModelAndView handleRuntimeException(RuntimeException e) {
		System.out.println("RuntimeException발생");
		e.printStackTrace();

		ModelAndView model = new ModelAndView();
		model.addObject("exceptionMsg", "RuntimeException.class");
		model.setViewName("exception");
		return model;
	}

	@ExceptionHandler(IOException.class)
	public ModelAndView handleDataAccessException(IOException e) {
		System.out.println("IOException발생");
		e.printStackTrace();

		ModelAndView model = new ModelAndView();
		model.addObject("exceptionMsg", "IOException.class");
		return model;
	}

	@Override
	@ExceptionHandler(BaseException.class)
	public ModelAndView handleBaseException(BaseException e) {
		return null;
	}

	@Override
	@ExceptionHandler(EgovBizException.class)
	public ModelAndView handleEgovBizException(EgovBizException e) {
		return null;
	}

	@Override
	@ExceptionHandler(FdlException.class)
	public ModelAndView handleFdlException(FdlException e) {
		return null;
	}

}
