/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.ptl.mvc.bind.exception;

import org.egovframe.rte.fdl.cmmn.exception.BaseException;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Added since egovframework .
 * @author 이영지
 *
 */
public abstract class AbstractAnnotationExceptionHandler {

	/** 
	 * Default Exeption handler.
	 * @param e Exception class type
	 * @return ModelAndView object
	 */
	@ExceptionHandler(Exception.class)
	public abstract ModelAndView handleException(Exception e);

	/**
	 * Default RuntimeException handler.
	 * @param e RuntimeException class type
	 * @return ModelAndView object
	 */
	@ExceptionHandler(RuntimeException.class)
	public abstract ModelAndView handleRuntimeException(RuntimeException e);

	/**
	 * BaseException handler.
	 * @param e BaseException class type
	 * @return ModelAndView object
	 */
	@ExceptionHandler(BaseException.class)
	public abstract ModelAndView handleBaseException(BaseException e);

	/**
	 * EgovBizException handler.
	 * @param e EgovBizException class type
	 * @return ModelAndView object
	 */
	@ExceptionHandler(EgovBizException.class)
	public abstract ModelAndView handleEgovBizException(EgovBizException e);

	/**
	 * FdlException handler.
	 * @param e FdlException class type
	 * @return ModelAndView object
	 */
	@ExceptionHandler(FdlException.class)
	public abstract ModelAndView handleFdlException(FdlException e);

}
