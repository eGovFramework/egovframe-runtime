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
package org.egovframe.rte.itl.webservice.data;

import org.egovframe.rte.itl.integration.metadata.ServiceDefinition;
import org.egovframe.rte.itl.integration.util.Validatable;
import org.springframework.util.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 전자정부 웹 서비스 설정 정보 중 Server 정보 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 웹 서비스 설정 정보 중 Server 정보를 나타내는 class이다. </p>
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
public class WebServiceServerDefinition implements Validatable {

	/** 서비스 key */
	private String key;

	/** ServiceDefinition */
	private ServiceDefinition serviceDefinition;

	/** namespace */
	private String namespace;

	/** address */
	private String address;

	/** service name */
	private String serviceName;

	/** port name */
	private String portName;

	/** operation name */
	private String operationName;

	/** valid */
	private boolean valid = false;

	/** statucChanged flag */
	private AtomicBoolean statusChanged = new AtomicBoolean(false);

	/**
	 * Default Constructor
	 */
	public WebServiceServerDefinition() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param key
	 *            서비스 key
	 * @param serviceDefinition
	 *            ServiceDefinition
	 * @param namespace
	 *            namespace
	 * @param address
	 *            address
	 * @param serviceName
	 *            service name
	 * @param portName
	 *            port name
	 * @param operationName
	 *            operation name
	 */
	public WebServiceServerDefinition(String key,
			ServiceDefinition serviceDefinition, String namespace,
			String address, String serviceName, String portName,
			String operationName) {
		super();
		this.key = key;
		this.serviceDefinition = serviceDefinition;
		this.namespace = namespace;
		this.address = address;
		this.serviceName = serviceName;
		this.portName = portName;
		this.operationName = operationName;
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
	 * serviceDefinition
	 * 
	 * @return the serviceDefinition
	 */
	public ServiceDefinition getServiceDefinition() {
		return serviceDefinition;
	}

	/**
	 * serviceDefinition
	 * 
	 * @param serviceDefinition
	 *            the serviceDefinition to set
	 */
	public void setServiceDefinition(ServiceDefinition serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
		this.statusChanged.set(true);
	}

	/**
	 * namespace
	 * 
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * namespace
	 * 
	 * @param namespace
	 *            the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
		this.statusChanged.set(true);
	}

	/**
	 * address
	 * 
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * address
	 * 
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
		this.statusChanged.set(true);
	}

	/**
	 * serviceName
	 * 
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * serviceName
	 * 
	 * @param serviceName
	 *            the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
		this.statusChanged.set(true);
	}

	/**
	 * portName
	 * 
	 * @return the portName
	 */
	public String getPortName() {
		return portName;
	}

	/**
	 * portName
	 * 
	 * @param portName
	 *            the portName to set
	 */
	public void setPortName(String portName) {
		this.portName = portName;
		this.statusChanged.set(true);
	}

	/**
	 * operationName
	 * 
	 * @return the operationName
	 */
	public String getOperationName() {
		return operationName;
	}

	/**
	 * operationName
	 * 
	 * @param operationName
	 *            the operationName to set
	 */
	public void setOperationName(String operationName) {
		this.operationName = operationName;
		this.statusChanged.set(true);
	}

	public boolean isValid() {
		if (statusChanged.getAndSet(false)) {
			valid = (StringUtils.hasText(key) && serviceDefinition != null
					&& StringUtils.hasText(namespace)
					&& StringUtils.hasText(address)
					&& StringUtils.hasText(serviceName)
					&& StringUtils.hasText(portName) && StringUtils.hasText(operationName));
			if (serviceDefinition != null) {
				valid = valid && serviceDefinition.isValid();
			}
		}
		return valid;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName())
				.append(" {")
				.append("\n\tkey = ")
				.append(StringUtils.quote(key))
				.append("\n\tserviceDefinition = ").append(serviceDefinition)
				.append("\n\tnamespace = ")
				.append(StringUtils.quote(namespace)).append("\n\taddress = ")
				.append(StringUtils.quote(address))
				.append("\n\tserviceName = ")
				.append(StringUtils.quote(serviceName))
				.append("\n\tportName = ").append(StringUtils.quote(portName))
				.append("\n\toperationName = ")
				.append(StringUtils.quote(operationName)).append("\n}");
		return sb.toString();
	}

}
