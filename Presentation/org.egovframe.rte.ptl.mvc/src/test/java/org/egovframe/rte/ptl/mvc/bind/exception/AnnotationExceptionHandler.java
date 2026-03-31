package org.egovframe.rte.ptl.mvc.bind.exception;

import org.egovframe.rte.fdl.cmmn.exception.BaseException;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class AnnotationExceptionHandler extends AbstractAnnotationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationExceptionHandler.class);

    @Override
    public ModelAndView handleException(Exception e) {
        ModelAndView model = new ModelAndView();
        model.addObject("exceptionMsg", "Exception.class");
        model.setViewName("exception");
        model.setStatus(HttpStatus.NOT_FOUND);
        return model;
    }

    @Override
    public ModelAndView handleRuntimeException(RuntimeException e) {
        ModelAndView model = new ModelAndView();
        model.addObject("exceptionMsg", "RuntimeException.class");
        model.setViewName("exception");
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
