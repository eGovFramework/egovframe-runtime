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
package egovframework.rte.itl.webservice.data;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import egovframework.rte.itl.integration.metadata.ServiceDefinition;
import egovframework.rte.itl.integration.util.Validatable;

import org.springframework.util.StringUtils;

/**
 * 전자정부 웹 서비스 설정 정보 중 Client 정보 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 웹 서비스 설정 정보 중 Client 정보를 나타내는 class이다. </p>
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
public class WebServiceClientDefinition implements Validatable {
	/** 서비스 Key */
	private String key;

	/** ServiceDefinition */
	private ServiceDefinition serviceDefinition;

	/** namespace */
	private String namespace;

	/** wsdl address */
	private String wsdlAddress;

	/** service name */
	private String serviceName;

	/** port name */
	private String portName;

	/** operation name */
	private String operationName;

	/** request mapping info */
	private Map<String, MappingInfo> requestMappingInfos;

	/** response mapping info */
	private Map<String, MappingInfo> responseMappingInfos;

	/** valid */
	private boolean valid = false;

	/** statucChanged flag */
	private AtomicBoolean statusChanged = new AtomicBoolean(false);

	/**
	 * Default Constructor
	 */
	public WebServiceClientDefinition() {
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
	 * @param wsdlAddress
	 *            wsdl address
	 * @param serviceName
	 *            service name
	 * @param portName
	 *            port name
	 * @param operationName
	 *            operation name
	 * @param requestMappingInfos
	 *            request mapping info
	 * @param responseMappingInfos
	 *            response mapping info
	 */
	public WebServiceClientDefinition(String key,
			ServiceDefinition serviceDefinition, String namespace,
			String wsdlAddress, String serviceName, String portName,
			String operationName, Map<String, MappingInfo> requestMappingInfos,
			Map<String, MappingInfo> responseMappingInfos) {
		super();
		this.key = key;
		this.serviceDefinition = serviceDefinition;
		this.namespace = namespace;
		this.wsdlAddress = wsdlAddress;
		this.serviceName = serviceName;
		this.portName = portName;
		this.operationName = operationName;
		this.requestMappingInfos = requestMappingInfos;
		this.responseMappingInfos = responseMappingInfos;
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
	 * wsdlAddress
	 * 
	 * @return the wsdlAddress
	 */
	public String getWsdlAddress() {
		return wsdlAddress;
	}

	/**
	 * wsdlAddress
	 * 
	 * @param wsdlAddress
	 *            the wsdlAddress to set
	 */
	public void setWsdlAddress(String wsdlAddress) {
		this.wsdlAddress = wsdlAddress;
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
	 * portName
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

	/**
	 * requestMappingInfos
	 * 
	 * @return the requestMappingInfos
	 */
	public Map<String, MappingInfo> getRequestMappingInfos() {
		return requestMappingInfos;
	}

	/**
	 * requestMappingInfos
	 * 
	 * @param requestMappingInfos
	 *            the requestMappingInfos to set
	 */
	public void setRequestMappingInfos(
			Map<String, MappingInfo> requestMappingInfos) {
		this.requestMappingInfos = requestMappingInfos;
		this.statusChanged.set(true);
	}

	/**
	 * responseMappingInfos
	 * 
	 * @return the responseMappingInfos
	 */
	public Map<String, MappingInfo> getResponseMappingInfos() {
		return responseMappingInfos;
	}

	/**
	 * responseMappingInfos
	 * 
	 * @param responseMappingInfos
	 *            the responseMappingInfos to set
	 */
	public void setResponseMappingInfos(
			Map<String, MappingInfo> responseMappingInfos) {
		this.responseMappingInfos = responseMappingInfos;
		this.statusChanged.set(true);
	}

	public boolean isValid() {
		if (statusChanged.getAndSet(false)) {
			valid = (StringUtils.hasText(key) && serviceDefinition != null
					&& StringUtils.hasText(namespace)
					&& StringUtils.hasText(wsdlAddress)
					&& StringUtils.hasText(serviceName)
					&& StringUtils.hasText(portName) && StringUtils.hasText(operationName));
			if (serviceDefinition != null) {
				valid = valid && serviceDefinition.isValid();
			}
			if (requestMappingInfos != null) {
				for (MappingInfo mappingInfo : requestMappingInfos.values()) {
					valid = valid && mappingInfo.isValid();
				}
			}
			if (responseMappingInfos != null) {
				for (MappingInfo mappingInfo : responseMappingInfos.values()) {
					valid = valid && mappingInfo.isValid();
				}
			}
		}
		return valid;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(this.getClass().getName()).append(" {").append("\n\tkey = ")
			
				.append(StringUtils.quote(key))
				.append("\n\tserviceDefinition = ").append(serviceDefinition)
				.append("\n\tnamespace = ")
				.append(StringUtils.quote(namespace))
				.append("\n\twsdlAddress = ")
				.append(StringUtils.quote(wsdlAddress))
				.append("\n\tserviceName = ")
				.append(StringUtils.quote(serviceName))
				.append("\n\tportName = ").append(StringUtils.quote(portName))
				.append("\n\toperationName = ")
				.append(StringUtils.quote(operationName));
		
		if (requestMappingInfos == null) {
			sb.append("\n\trequestMappingInfos = null");
		} else {
			sb.append("\n\trequestMappingInfos = {");
		
			for (Entry<String, MappingInfo> entry : requestMappingInfos.entrySet()) {
				sb.append("\n\t\t<key = ")
						.append(StringUtils.quote(entry.getKey()))
						.append(", value = ").append(entry.getValue())
						.append(">");
			}
			sb.append("\n\t}");
		}
		if (responseMappingInfos == null) {
			sb.append("\n\tresponseMappingInfos = null");
		} else {
			sb.append("\n\tresponseMappingInfos = {");
			for (Entry<String, MappingInfo> entry : responseMappingInfos.entrySet()) {
				sb.append("\n\t\t<key = ")
						.append(StringUtils.quote(entry.getKey()))
						.append(", value = ").append(entry.getValue()).append(">");
			}
			sb.append("\n\t}");
		}
		sb.append("\n}");
		// CHECKSTYLE:ON
		
		return sb.toString();
	}
}
