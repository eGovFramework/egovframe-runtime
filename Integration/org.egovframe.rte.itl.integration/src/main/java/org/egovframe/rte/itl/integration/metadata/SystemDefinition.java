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

import org.egovframe.rte.itl.integration.util.Validatable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 전자정부 연계 서비스 메타 데이터 중 '시스템'를 나타내는 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 메타 데이터 중 '시스템'를 나타내는 class이다.
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
public class SystemDefinition implements Validatable {

	/** key */
	private String key;

	/** 기관 */
	private OrganizationDefinition organization;

	/** 시스템 ID */
	private String id;

	/** 시스템명 */
	private String name;

	/** 표준 여부 */
	private boolean standard;

	/** 소속 서비스 */
	private Map<String, ServiceDefinition> services = new HashMap<String, ServiceDefinition>();

	/** valid */
	private boolean valid = false;

	/** statucChanged flag */
	private AtomicBoolean statusChanged = new AtomicBoolean(false);

	/**
	 * Default Constructor
	 */
	public SystemDefinition() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param key
	 *            key
	 * @param organization
	 *            기관
	 * @param id
	 *            시스템 ID
	 * @param name
	 *            시스템명
	 * @param standard
	 *            표준 여부
	 */
	public SystemDefinition(String key, OrganizationDefinition organization,
			String id, String name, boolean standard) {
		super();
		this.key = key;
		this.organization = organization;
		this.id = id;
		this.name = name;
		this.standard = standard;
		this.statusChanged.set(true);
	}

	/**
	 * Constructor
	 * 
	 * @param key
	 *            key
	 * @param organization
	 *            기관
	 * @param id
	 *            시스템 ID
	 * @param name
	 *            시스템명
	 * @param standard
	 *            표준 여부
	 * @param services
	 *            소속 서비스
	 */
	public SystemDefinition(String key, OrganizationDefinition organization,
			String id, String name, boolean standard,
			Map<String, ServiceDefinition> services) {
		super();
		this.key = key;
		this.organization = organization;
		this.id = id;
		this.name = name;
		this.standard = standard;
		this.services = services;
		this.statusChanged.set(true);
	}

	/**
	 * Key
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Key
	 * 
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
		this.statusChanged.set(true);
	}

	/**
	 * 기관
	 * 
	 * @return the organization
	 */
	public OrganizationDefinition getOrganization() {
		return organization;
	}

	/**
	 * 기관
	 * 
	 * @param organization
	 *            the organization to set
	 */
	public void setOrganization(OrganizationDefinition organization) {
		this.organization = organization;
		this.statusChanged.set(true);
	}

	/**
	 * 시스템 Id
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * 시스템 Id
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
		this.statusChanged.set(true);
	}

	/**
	 * 시스템 명
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 시스템 명
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
		this.statusChanged.set(true);
	}

	/**
	 * 표준 여부
	 * 
	 * @return the standard
	 */
	public boolean isStandard() {
		return standard;
	}

	/**
	 * 표준 여부
	 * 
	 * @param standard
	 *            the standard to set
	 */
	public void setStandard(boolean standard) {
		this.standard = standard;
		// this.statusChanged.set(true);
	}

	/**
	 * 소속서비스
	 * 
	 * @return the services
	 */
	public Map<String, ServiceDefinition> getServices() {
		return services;
	}

	/**
	 * 소속서비스
	 * 
	 * @param services
	 *            the services to set
	 */
	public void setServices(Map<String, ServiceDefinition> services) {
		this.services = services;
		this.statusChanged.set(true);
	}

	public ServiceDefinition getServiceDefinition(String serviceId) {
		return services.get(serviceId);
	}

	public boolean isValid() {
		if (statusChanged.getAndSet(false)) {
			valid = (StringUtils.hasText(key) && organization != null
					&& StringUtils.hasText(id) && StringUtils.hasText(name) && services != null);
			if (organization != null) {
				valid = valid && organization.isValid();
			}
			if (services != null) {
				for (ServiceDefinition service : services.values()) {
					valid = valid && service.isValid();
				}
			}
		}
		return valid;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName()).append(" {").append("\n\tkey = ")
				.append(StringUtils.quote(key));
		if (organization == null) {
			sb.append("\n\torganization = null");
		} else {
			sb.append("\n\torganization.id = ").append(
					StringUtils.quote(organization.getId()));
		}
		sb.append("\n\tid = ").append(StringUtils.quote(id))
				.append("\n\tname = ").append(StringUtils.quote(name))
				.append("\n\tstandard = ").append(standard);
		if (services == null) {
			sb.append("\n\tservices = null");
		} else {
			sb.append("\n\tservices = {");
			for (Entry<String, ServiceDefinition> entry : services.entrySet()) {
				sb.append("\n\t\t<key = ")
						.append(StringUtils.quote(entry.getKey()))
						.append(", value = ")
						.append(entry.getValue() == null ? "" : "\n")
						.append(entry.getValue()).append(">");
			}
			sb.append("\n\t}");
		}
		sb.append("\n}");
		return sb.toString();
	}

}
