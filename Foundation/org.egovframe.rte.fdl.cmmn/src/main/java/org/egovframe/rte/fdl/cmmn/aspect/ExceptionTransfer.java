/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.rte.fdl.cmmn.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.egovframe.rte.fdl.cmmn.exception.BaseException;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.cmmn.exception.manager.ExceptionHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * Exception 발생시 AOP(after-throwing) 에 의해 후처리로직 연결고리 역할 수행하는 클래스이다.
 *
 * <p><b>NOTE:</b> Exception 종류를 EgovBizException, RuntimeException(DataAccessException 포함), FdlException ,
 * 나머지 Exception 으로 나누고 있으며, 후처리로직은 EgovBizException, RuntimeException 에서만 동작한다.
 * 그리고 나머지 Exception 의 경우 Exception 을 BaseException (메세지: fail.common.msg)으로 재생성하여 변경 던진다.
 * 따라서 fail.common.msg 메세지키가 Message Resource 에 정의 되어 있어야 한다.</p>
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	Judd Cho		최초 생성
 * 2015.01.31	Vincent Han		코드 품질 개선 및 보완 (processHandling 메소드 Exception 처리)
 * 2020.08.31	ESFC			시큐어코딩(ES)-Private 배열에 Public 데이터 할당[CWE-496]
 * </pre>
 */
public class ExceptionTransfer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionTransfer.class);

	@Resource(name = "messageSource")
	private MessageSource messageSource;

	private ExceptionHandlerService[] exceptionHandlerServices;

	/**
	 * 디볼트로 패턴 매칭은 ANT 형태로 비교한다.
	 */
	private PathMatcher pm = new AntPathMatcher();

	/**
	 * ExceptionHandlerService을 여러개 지정한다.
	 * @param exceptionHandlerServices array of HandlerService
	 */
	public void setExceptionHandlerService(ExceptionHandlerService[] exceptionHandlerServices) {
		// 2020.08.31 ESFC 시큐어코딩(ES)-Private 배열에 Public 데이터 할당[CWE-496]
		this.exceptionHandlerServices = new ExceptionHandlerService[exceptionHandlerServices.length];
		for (int i = 0; i < exceptionHandlerServices.length; i++) {
			this.exceptionHandlerServices[i] = exceptionHandlerServices[i];
		}
		if (this.exceptionHandlerServices != null) {
			LOGGER.debug("count of ExceptionHandlerServices = {}", exceptionHandlerServices.length);
		}
	}

	/**
	 * ExceptionHandlerService을 여러개 지정한다.
	 * @return int ExceptionHandlerService 갯수
	 */
	public int countOfTheExceptionHandlerService() {
		return exceptionHandlerServices != null ? exceptionHandlerServices.length : 0;
	}

	/**
	 * 발생한 Exception 에 따라 후처리 로직이 실행할 수 있도록 연결하는 역할을 수행한다.
	 * @param thisJoinPoint joinPoint 객체
	 * @param exception 발생한 Exception
	 */
	public void transfer(JoinPoint thisJoinPoint, Exception exception) throws Exception {
		LOGGER.debug("execute ExceptionTransfer.transfer ");
		Class<?> clazz = thisJoinPoint.getTarget().getClass();
		Signature signature = thisJoinPoint.getSignature();
		Locale locale = LocaleContextHolder.getLocale();
		/*
		 * BizException 을 구분하여 후처리로직을 수행하려 했으나 고려해야 할 부분이 발생.
		 * Exception 구분하여 후처리 로직을 발생하려면 설정상에 Exception의 상세 설정이 필요하게된다.
		 * 하지만 실제 현장에서 그렇게 나누는 경우는 없다.
		 * 클래스 정보로만 패턴 분석을 통해 Handler 로 연결해주는 고리 역할 수행을 하게 된다.
		 */
		//EgovBizException 이 발생시
		if (exception instanceof EgovBizException) {
			LOGGER.debug("Exception case :: EgovBizException ");
			EgovBizException be = (EgovBizException) exception;
			//wrapp 된 Exception 있는 경우 error 원인으로 출력해준다.
			if (be.getWrappedException() != null) {
				Throwable throwable = be.getWrappedException();
				getLog(clazz).error(be.getMessage(), throwable);
			} else {
				getLog(clazz).error(be.getMessage(), be.getCause());
			}
			// Exception Handler 에 발생된 Package 와 Exception 설정. (runtime 이 아닌 ExceptionHandlerService를 실행함)
			processHandling(clazz, signature.getName(), be, pm, exceptionHandlerServices);
			throw be;
			//RuntimeException 이 발생시 내부에서 DataAccessException 인 경우 는 별도록 throw 하고 있다.
		} else if (exception instanceof RuntimeException) {
			LOGGER.debug("RuntimeException case :: RuntimeException ");
			RuntimeException be = (RuntimeException) exception;
			getLog(clazz).error(be.getMessage(), be.getCause());
			// Exception Handler 에 발생된 Package 와 Exception 설정.
			processHandling(clazz, signature.getName(), exception, pm, exceptionHandlerServices);
			if (be instanceof DataAccessException) {
				LOGGER.debug("RuntimeException case :: DataAccessException ");
				DataAccessException sqlEx = (DataAccessException) be;
				throw sqlEx;
			}
			throw be;
			//실행환경 확장모듈에서 발생한 Exception (요청: 공통모듈) :: 후처리로직 실행하지 않음.
		} else if (exception instanceof FdlException) {
			LOGGER.debug("FdlException case :: FdlException ");
			FdlException fe = (FdlException) exception;
			getLog(clazz).error(fe.getMessage(), fe.getCause());
			throw fe;
		} else {
			//그외에 발생한 Exception 을  BaseException (메세지: fail.common.msg) 로  만들어 변경 던진다.
			//후처리로직 실행하지 않음.
			LOGGER.debug("case :: Exception ");
			getLog(clazz).error(exception.getMessage(), exception.getCause());
			throw processException(clazz, "fail.common.msg", new String[] {}, exception, locale);
		}
	}
	
	/**
	 * Logger 얻기.
	 * @param clazz the returned logger will be named after clazz
	 * @return logger
	 */
	protected Logger getLog(Class<?> clazz) {
		return LoggerFactory.getLogger(clazz);
	}

	/**
	 * 발생한 Exception 에 따라 후처리 로직이 실행할 수 있도록 연결하는 역할을 수행한다.
	 * @param clazz Exception 발생 클래스
	 * @param msgKey Message key
	 * @param msgArgs Message arguments 
	 * @param e Exception
	 * @param locale Locale
	 * @return 메시지 처리된 Exception
	 */
	protected Exception processException(final Class<?> clazz, final String msgKey, final String[] msgArgs, final Exception e, final Locale locale) {
		return processException(clazz, msgKey, msgArgs, e, locale, null);
	}

	/**
	 * 발생한 Exception 에 따라 후처리 로직이 실행할 수 있도록 연결하는 역할을 수행한다.
	 * @param clazz Exception 발생 클래스
	 * @param msgKey Message key
	 * @param msgArgs Message arguments 
	 * @param e Exception
	 * @param locale Locale
	 * @param ec ExceptionCreator
	 * @return 메시지 처리된 Exception
	 */
	protected Exception processException(final Class<?> clazz, final String msgKey, final String[] msgArgs, final Exception e, final Locale locale, final ExceptionCreator ec) {
		getLog(clazz).error(messageSource.getMessage(msgKey, msgArgs, locale), e);
		ExceptionCreator exceptionCreator = null;
		if (ec == null) {
			exceptionCreator = new ExceptionCreator() {
				public Exception processException(MessageSource messageSource) {
					return new BaseException(messageSource, msgKey, msgArgs, locale, e);
				}
			};
		} else {
			exceptionCreator = ec;
		}
		return exceptionCreator.processException(messageSource);
	}

	/**
	 * Exception 생성 인터페이스이다.
	 */
	protected interface ExceptionCreator {
		Exception processException(MessageSource messageSource);
	}

	/**
	 * 발생한 Exception 에 따라 후처리 로직이 실행할 수 있도록 연결하는 역할을 수행한다.
	 * @param clazz Exception 발생 클래스
	 * @param methodName Exception 발생 메소드명
	 * @param exception 발생한 Exception
	 * @param pm 발생한 PathMatcher(default : AntPathMatcher)
	 * @param exceptionHandlerServices 등록되어 있는 ExceptionHandlerService 리스트
	 */
	protected void processHandling(Class<?> clazz, String methodName, Exception exception, PathMatcher pm, ExceptionHandlerService[] exceptionHandlerServices) {
		for (ExceptionHandlerService ehm : exceptionHandlerServices) {
			try {
				if (!ehm.hasReqExpMatcher()) {
					ehm.setReqExpMatcher(pm);
				}
				ehm.setPackageName(clazz.getCanonicalName() + "." + methodName);
				ehm.run(exception);
			} catch (Exception e) {
				LOGGER.error("ExceptionHandlerService Error", e);
			}
		}
	}

}
