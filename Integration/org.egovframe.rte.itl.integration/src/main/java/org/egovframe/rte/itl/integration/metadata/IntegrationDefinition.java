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
package org.egovframe.rte.itl.integration.metadata;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import org.egovframe.rte.itl.integration.util.Validatable;

import org.springframework.util.StringUtils;

/**
 * 연계 서비스 메타 데이터 중 '연계 정의' 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 메타 데이터 중 '연계 정의'를 나타내는 class이다.
 * </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * </pre>
 */
public class IntegrationDefinition implements Validatable {

	/** 연계 ID */
	private String id;

	/** 제공 서비스 */
	private ServiceDefinition provider;

	/** 요청 시스템 */
	private SystemDefinition consumer;

	/** default timeout */
	private long defaultTimeout;

	/** 사용 여부 */
	private boolean using;

	/** 유효 기간 (시작) */
	private Calendar validateFrom;

	/** 유효 기간 (끝) */
	private Calendar validateTo;

	/** valid */
	private boolean valid = false;

	/** statucChanged flag */
	private AtomicBoolean statusChanged = new AtomicBoolean(false);

	/**
	 * Default Constructor
	 */
	public IntegrationDefinition() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            연계 ID
	 * @param provider
	 *            제공 서비스
	 * @param consumer
	 *            요청 시스템
	 * @param defaultTimeout
	 *            default timeout
	 * @param using
	 *            사용 여부
	 * @param validateFrom
	 *            유효 기간 (시작)
	 * @param validateTo
	 *            유효 기간 (끝)
	 */
	public IntegrationDefinition(String id, ServiceDefinition provider,
			SystemDefinition consumer, long defaultTimeout, boolean using,
			Calendar validateFrom, Calendar validateTo) {
		super();
		this.id = id;
		this.provider = provider;
		this.consumer = consumer;
		this.defaultTimeout = defaultTimeout;
		this.using = using;
		this.validateFrom = validateFrom;
		this.validateTo = validateTo;
		this.statusChanged.set(true);
	}

	/**
	 * 연계 id
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * 연계 id
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
		this.statusChanged.set(true);
	}

	/**
	 * 제공서비스
	 * 
	 * @return the provider
	 */
	public ServiceDefinition getProvider() {
		return provider;
	}

	/**
	 * 제공서비스
	 * 
	 * @param provider
	 *            the provider to set
	 */
	public void setProvider(ServiceDefinition provider) {
		this.provider = provider;
		this.statusChanged.set(true);
	}

	/**
	 * 요청시스템
	 * 
	 * @return the consumer
	 */
	public SystemDefinition getConsumer() {
		return consumer;
	}

	/**
	 * 요청시스템
	 * 
	 * @param consumer
	 *            the consumer to set
	 */
	public void setConsumer(SystemDefinition consumer) {
		this.consumer = consumer;
		this.statusChanged.set(true);
	}

	/**
	 * defaultTimeout
	 * 
	 * @return the defaultTimeout
	 */
	public long getDefaultTimeout() {
		return defaultTimeout;
	}

	/**
	 * defaultTimeout
	 * 
	 * @param defaultTimeout
	 *            the defaultTimeout to set
	 */
	public void setDefaultTimeout(long defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
		// this.statusChanged.set(true);
	}

	/**
	 * 사용여부
	 * 
	 * @return the using
	 */
	public boolean isUsing() {
		return using;
	}

	/**
	 * 사용여부
	 * 
	 * @param using
	 *            the using to set
	 */
	public void setUsing(boolean using) {
		this.using = using;
		// this.statusChanged.set(true);
	}

	/**
	 * 유효기간(시작)
	 * 
	 * @return the validateFrom
	 */
	public Calendar getValidateFrom() {
		return validateFrom;
	}

	/**
	 * 유효기간(시작)
	 * 
	 * @param validateFrom
	 *            the validateFrom to set
	 */
	public void setValidateFrom(Calendar validateFrom) {
		this.validateFrom = validateFrom;
		// this.statusChanged.set(true);
	}

	/**
	 * 유효기간(끝)
	 * 
	 * @return the validateTo
	 */
	public Calendar getValidateTo() {
		return validateTo;
	}

	/**
	 * 유효기간(끝)
	 * 
	 * @param validateTo
	 *            the validateTo to set
	 */
	public void setValidateTo(Calendar validateTo) {
		this.validateTo = validateTo;
		// this.statusChanged.set(true);
	}

	public boolean isValid() {
		if (statusChanged.getAndSet(false)) {
			valid = (StringUtils.hasText(id) && provider != null && consumer != null);
			if (provider != null) {
				valid = valid && provider.isValid();
			}
			if (consumer != null) {
				valid = valid && consumer.isValid();
			}
		}
		return valid;
	}

	// CHECKSTYLE:OFF
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName()).append(" {").append("\n\tid = ")
				.append(StringUtils.quote(id)).append("\n\tprovider = ")
				.append(provider == null ? "" : "\n").append(provider)
				.append("\n\tconsumer = ").append(consumer == null ? "" : "\n")
				.append(consumer).append("\n\tdefaultTimeout = ")
				.append(defaultTimeout).append("\n\tusing = ").append(using)
				.append("\n\tvalidateFrom = ").append(validateFrom)
				.append("\n\tvalidateTo = ").append(validateTo).append("\n}");
		return sb.toString();
	}
	// CHECKSTYLE:ON

}
