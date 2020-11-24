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
package org.egovframe.rte.fdl.idgnr.impl;

import java.math.BigDecimal;
import java.util.Locale;

import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

/**
 * ID Generation 서비스를 위한 Abstract Service
 * 
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.02.01   김태호             최초 생성
 * </pre>
 */
public abstract class AbstractIdGnrService implements EgovIdGnrService, ApplicationContextAware, BeanFactoryAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIdGnrService.class);

	/**
	 * BeanFactory
	 */
	private BeanFactory beanFactory;

	/**
	 * BIG_DECIMAL_MAX_LONG 정의
	 */
	private static final BigDecimal BIG_DECIMAL_MAX_LONG = new BigDecimal(Long.MAX_VALUE);

	/**
	 * 내부 synchronization을 위한 정보
	 */
	private final Object mSemaphore = new Object();

	/**
	 * 정책정보 생성
	 */
	private EgovIdGnrStrategy strategy = new EgovIdGnrStrategy() {
		public String makeId(String originalId) {
			return originalId;
		}
	};

	/**
	 * BigDecimal 사용 여부
	 */
	protected boolean useBigDecimals = false;

	/**
	 * MessageSource
	 */
	protected MessageSource messageSource;

	/**
	 * 기본 생성자
	 */
	public AbstractIdGnrService() {
	}

	/**
	 * BigDecimal 타입의 유일 아이디 제공
	 * @return BigDecimal 타입의 다음 ID
	 * @throws FdlException if an Id could not be allocated for any reason.
	 */
	protected abstract BigDecimal getNextBigDecimalIdInner() throws FdlException;

	/**
	 * long 타입의 유일 아이디 제공
	 * @return long 타입의 다음 ID
	 * @throws FdlException 여타이유에 의해 아이디 생성이 불가능 할때
	 */
	protected abstract long getNextLongIdInner() throws FdlException;

	/**
	 * BigDecimal 사용여부 세팅
	 * @param useBigDecimals BigDecimal 사용여부
	 */
	public final void setUseBigDecimals(boolean useBigDecimals) {
		this.useBigDecimals = useBigDecimals;
	}

	/**
	 * BigDecimal 사용여부 정보 리턴
	 * @return boolean check using BigDecimal
	 */
	protected final boolean isUsingBigDecimals() {
		return useBigDecimals;
	}

	/**
	 * 특별한 최대 값보다 작은 Long 타입의 다음 ID
	 * @param maxId 최대값
	 * @return long value to be less than the specified maxId
	 * @throws FdlException 다음 ID가 입력받은 MaxId보다 클때
	 */
	protected final long getNextLongIdChecked(long maxId) throws FdlException {
		long nextId;
		if (useBigDecimals) {
			BigDecimal bd;
			synchronized (mSemaphore) {
				bd = getNextBigDecimalIdInner();
			}

			if (bd.compareTo(BIG_DECIMAL_MAX_LONG) > 0) {
				LOGGER.error(messageSource.getMessage("error.idgnr.greater.maxid", new String[] { "Long" }, Locale.getDefault()));
				throw new FdlException(messageSource, "error.idgnr.greater.maxid");
			}
			nextId = bd.longValue();
		} else {
			synchronized (mSemaphore) {
				nextId = getNextLongIdInner();
			}
		}

		if (nextId > maxId) {
			LOGGER.error(messageSource.getMessage("error.idgnr.greater.maxid", new String[] { "Long" }, Locale.getDefault()));
			throw new FdlException(messageSource, "error.idgnr.greater.maxid");
		}

		return nextId;
	}

	/**
	 * Returns BigDecimal 타입의 다음 ID 제공
	 * @return BigDecimal the next Id.
	 * @throws FdlException 다음 아이디가 유효한 BigDecimal의 범위를 벗어날때
	 */
	public final BigDecimal getNextBigDecimalId() throws FdlException {
		BigDecimal bd;
		if (useBigDecimals) {
			synchronized (mSemaphore) {
				bd = getNextBigDecimalIdInner();
			}
		} else {
			synchronized (mSemaphore) {
				bd = new BigDecimal(getNextLongIdInner());
			}
		}
		return bd;
	}

	/**
	 * Returns long 타입의 다음 ID 제공
	 * @return the next Id.
	 * @throws FdlException 다음 아이디가 유효한 long의 범위를 벗어날때
	 */
	public final long getNextLongId() throws FdlException {
		return getNextLongIdChecked(Long.MAX_VALUE);
	}

	/**
	 * Returns int 타입의 다음 ID 제공
	 * @return the next Id.
	 * @throws FdlException 다음 아이디가 유효한 integer의 범위를 벗어날때
	 */
	public final int getNextIntegerId() throws FdlException {
		return (int) getNextLongIdChecked(Integer.MAX_VALUE);
	}

	/**
	 * Returns Short 타입의 다음 ID 제공
	 * @return the next Id.
	 * @throws FdlException 다음 아이디가 유효한 Short의 범위를 벗어날때
	 */
	public final short getNextShortId() throws FdlException {
		return (short) getNextLongIdChecked(Short.MAX_VALUE);
	}

	/**
	 * Returns Byte 타입의 다음 ID 제공
	 * @return the next Id.
	 * @throws FdlException 다음 아이디가 유효한 Byte 범위를 벗어날때
	 */
	public final byte getNextByteId() throws FdlException {
		return (byte) getNextLongIdChecked(Byte.MAX_VALUE);
	}

	/**
	 * String 타입의 Id 제공하는데 정책의 아이디 생성 호출함
	 * @return the next Id.
	 * @throws FdlException 다음 아이디가 유효한 byte의 범위를 벗어날때
	 */
	public final String getNextStringId() throws FdlException {
		return strategy.makeId(getNextBigDecimalId().toString());
	}

	/**
	 * 정책 클래스를 입력받아서 String 타입의 Id 제공
	 * @param strategy 생성된 정책 오브젝트
	 * @return the next Id.
	 * @throws FdlException 다음 아이디가 유효한 byte의 범위를 벗어날때
	 */
	public String getNextStringId(EgovIdGnrStrategy strategy) throws FdlException {
		this.strategy = strategy;
		return getNextStringId();
	}

	/**
	 * 정책정보를 String으로 입력받아서 String 타입의 Id 제공
	 * @param strategyId 정책 스트링
	 * @return the next Id.
	 * @throws FdlException 다음 아이디가 유효한 byte의 범위를 벗어날때
	 */
	public String getNextStringId(String strategyId) throws FdlException {
		this.strategy = (EgovIdGnrStrategy) this.beanFactory.getBean(strategyId);
		return getNextStringId();
	}

	/**
	 * 정책 얻기
	 * @return IdGenerationStrategy
	 */
	public EgovIdGnrStrategy getStrategy() {
		return strategy;
	}

	/**
	 * 정책 세팅
	 * @param strategy to be set by Spring Framework
	 */
	public void setStrategy(EgovIdGnrStrategy strategy) {
		this.strategy = strategy;
	}

	/**
	 * set BeanFactory
	 * @param beanFactory to be set by Spring Framework
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Message Source Injection
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.messageSource = (MessageSource) applicationContext.getBean("messageSource");
	}

}
