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
package egovframework.rte.itl.integration.metadata;

import java.util.concurrent.atomic.AtomicBoolean;

import egovframework.rte.itl.integration.util.Validatable;

import org.springframework.util.StringUtils;

/**
 * 연계 서비스 메타 데이터 중 '서비스'를 정의하기 위한 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 메타 데이터 중 '서비스'를 정의하기 위한 Class이다. <br>
 * 모든 서비스를 고유한 <code>id</code>를 가지고 있으며, 반드시 요청 메시지 Type Id (
 * <code>requestMessageTypeId</code>)와 응답 메시지 Type Id (
 * <code>responseMessageTypeId</code>)를 가져야 한다. Attribute
 * <code>serviceProviderBeanId</code>는 서비스를 제공하는 객체의 id로 Spring Framework의
 * Application Context에 등록되어 있는 bean id이다. 만약 <code>null</code> 값인 경우 외부에서 제공받는
 * 서비스를 의미한다.
 * </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.06.01  심상호           최초 생성
 * 
 * </pre>
 */
public class ServiceDefinition implements Validatable {

	/** Key */
	private String key;

	/** 시스템 */
	private SystemDefinition system;

	/** 서비스 Id */
	private String id;

	/** 서비스 명 */
	private String name;

	/** 요청 메시지 Type Id */
	private String requestMessageTypeId;

	/** 응답 메시지 Type Id */
	private String responseMessageTypeId;

	/** 서비스 제공 Bean Id */
	private String serviceProviderBeanId;

	/** 표준여부 */
	private boolean standard;

	/** 사용여부 */
	private boolean using;

	/** valid */
	private boolean valid = false;

	/** statucChanged flag */
	private AtomicBoolean statusChanged = new AtomicBoolean(false);

	/**
	 * Default Constructor
	 */
	public ServiceDefinition() {
		super();
	}

	/**
	 * ServiceDefinition 객체를 생성한다.
	 * 
	 * @param key
	 *            KEY
	 * @param system
	 *            시스템
	 * @param id
	 *            서비스 Id
	 * @param name
	 *            서비스 명
	 * @param requestMessageTypeId
	 *            요청 메시지 Type Id
	 * @param responseMessageTypeId
	 *            응답 메시지 Type Id
	 * @param serviceProviderBeanId
	 *            서비스 제공 Bean Id
	 * @param standard
	 *            표준 여부
	 * @param using
	 *            사용 여부
	 */
	public ServiceDefinition(String key, SystemDefinition system, String id,
			String name, String requestMessageTypeId,
			String responseMessageTypeId, String serviceProviderBeanId,
			boolean standard, boolean using) {
		super();
		this.key = key;
		this.system = system;
		this.id = id;
		this.name = name;
		this.requestMessageTypeId = requestMessageTypeId;
		this.responseMessageTypeId = responseMessageTypeId;
		this.serviceProviderBeanId = serviceProviderBeanId;
		this.standard = standard;
		this.using = using;
		this.statusChanged.set(true);
	}

	/**
	 * key
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * key
	 * 
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
		this.statusChanged.set(true);
	}

	/**
	 * 시스템
	 * 
	 * @return the system
	 */
	public SystemDefinition getSystem() {
		return system;
	}

	/**
	 * 시스템
	 * 
	 * @param system
	 *            the system to set
	 */
	public void setSystem(SystemDefinition system) {
		this.system = system;
		this.statusChanged.set(true);
	}

	/**
	 * 서비스 Id
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * 서비스 Id
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
		this.statusChanged.set(true);
	}

	/**
	 * 서비스명
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 서비스명
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
		this.statusChanged.set(true);
	}

	/**
	 * 요청메시지 TypeId
	 * 
	 * @return the requestMessageTypeId
	 */
	public String getRequestMessageTypeId() {
		return requestMessageTypeId;
	}

	/**
	 * 요청메시지 TypeId
	 * 
	 * @param requestMessageTypeId
	 *            the requestMessageTypeId to set
	 */
	public void setRequestMessageTypeId(String requestMessageTypeId) {
		this.requestMessageTypeId = requestMessageTypeId;
		this.statusChanged.set(true);
	}

	/**
	 * 응답메시지 Type Id
	 * 
	 * @return the responseMessageTypeId
	 */
	public String getResponseMessageTypeId() {
		return responseMessageTypeId;
	}

	/**
	 * 응답메시지 Type Id
	 * 
	 * @param responseMessageTypeId
	 *            the responseMessageTypeId to set
	 */
	public void setResponseMessageTypeId(String responseMessageTypeId) {
		this.responseMessageTypeId = responseMessageTypeId;
		this.statusChanged.set(true);
	}

	/**
	 * 서비스 제공 Bean Id
	 * 
	 * @return the serviceProviderBeanId
	 */
	public String getServiceProviderBeanId() {
		return serviceProviderBeanId;
	}

	/**
	 * 서비스 제공 Bean Id
	 * 
	 * @param serviceProviderBeanId
	 *            the serviceProviderBeanId to set
	 */
	public void setServiceProviderBeanId(String serviceProviderBeanId) {
		this.serviceProviderBeanId = serviceProviderBeanId;
		this.statusChanged.set(true);
	}

	/**
	 * 표준여부
	 * 
	 * @return the standard
	 */
	public boolean isStandard() {
		return standard;
	}

	/**
	 * 표준여부
	 * 
	 * @param standard
	 *            the standard to set
	 */
	public void setStandard(boolean standard) {
		this.standard = standard;
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
//	CHECKSTYLE:OFF
	public boolean isValid() {
		if (statusChanged.getAndSet(false)) {
			valid = (StringUtils.hasText(key) && system != null
					&& StringUtils.hasText(id) && StringUtils.hasText(name)
					&& StringUtils.hasText(requestMessageTypeId) && StringUtils
					.hasText(responseMessageTypeId));
			if (system != null) {
				valid = valid && system.isValid();
			}
		}
		return valid;
	}
//	CHECKSTYLE:ON

//	CHECKSTYLE:OFF
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName()).append(" {").append("\n\tkey = ")
				.append(StringUtils.quote(key));
		if (system == null) {
			sb.append("\n\tsystem = null");
		} else {
			sb.append("\n\tsystem.key = ").append(
					StringUtils.quote(system.getKey()));
		}
		sb.append("\n\tid = ").append(StringUtils.quote(id))
				.append("\n\tname = ").append(StringUtils.quote(name))
				.append("\n\trequestMessageTypeId = ")
				.append(StringUtils.quote(requestMessageTypeId))
				.append("\n\tresponseMessageTypeId = ")
				.append(StringUtils.quote(responseMessageTypeId))
				.append("\n\tserivceProviderBeanId = ")
				.append(StringUtils.quote(serviceProviderBeanId))
				.append("\n\tusing = ").append(using).append("\n\tstandard = ")
				.append(standard).append("\n}");
		return sb.toString();
	}
//	CHECKSTYLE:ON
}
