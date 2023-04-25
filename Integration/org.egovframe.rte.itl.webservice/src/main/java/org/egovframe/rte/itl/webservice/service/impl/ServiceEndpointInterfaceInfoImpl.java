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
package org.egovframe.rte.itl.webservice.service.impl;

import org.egovframe.rte.itl.integration.type.RecordType;
import org.egovframe.rte.itl.integration.type.Type;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessageHeader;
import org.egovframe.rte.itl.webservice.data.MappingInfo;
import org.egovframe.rte.itl.webservice.data.WebServiceClientDefinition;
import org.egovframe.rte.itl.webservice.service.ServiceEndpointInterfaceInfo;
import org.egovframe.rte.itl.webservice.service.ServiceParamInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.jws.WebParam.Mode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 웹 서비스 ServiceEndpointInterface 정보 구현 클래스
 * <p>
 * <b>NOTE:</b> 웹 서비스 ServiceEndpointInterface 정보를 나타내는 class이다. </p>
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
public class ServiceEndpointInterfaceInfoImpl implements ServiceEndpointInterfaceInfo {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEndpointInterfaceInfoImpl.class);

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

	/** return info */
	private ServiceParamInfo returnInfo;

	/** param info */
	private Collection<ServiceParamInfo> paramInfos;

	/**
	 * Constructor
	 * 
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
	 * @param returnInfo
	 *            return info
	 * @param paramInfos
	 *            param info
	 * @throws IllegalArgumentException
	 *             Argument 값이 <code>null</code>인 경우
	 */
	public ServiceEndpointInterfaceInfoImpl(String namespace,
			String wsdlAddress, String serviceName, String portName,
			String operationName, ServiceParamInfo returnInfo,
			Collection<ServiceParamInfo> paramInfos) {
		super();
		if (StringUtils.hasText(namespace) == false) {
			LOGGER.error("Argument 'namespace' has no text ({})", namespace);
			throw new IllegalArgumentException();
		} else if (StringUtils.hasText(wsdlAddress) == false) {
			LOGGER.error("Argument 'wsdlAddress' has no text ({})", wsdlAddress);
			throw new IllegalArgumentException();
		} else if (StringUtils.hasText(serviceName) == false) {
			LOGGER.error("Argument 'serviceName' has no text ({})", serviceName);
			throw new IllegalArgumentException();
		} else if (StringUtils.hasText(portName) == false) {
			LOGGER.error("Argument 'portName' has no text ({})", portName);
			throw new IllegalArgumentException();
		} else if (StringUtils.hasText(operationName) == false) {
			LOGGER.error("Argument 'operationName' has no text ({})",
					operationName);
			throw new IllegalArgumentException();
		} else if (paramInfos == null) {
			LOGGER.error("Argument 'paramInfos' is null");
			throw new IllegalArgumentException();
		}
		this.namespace = namespace;
		this.wsdlAddress = wsdlAddress;
		this.serviceName = serviceName;
		this.portName = portName;
		this.operationName = operationName;
		this.returnInfo = returnInfo;
		this.paramInfos = paramInfos;
	}

	/**
	 * Constructor
	 * 
	 * @param webServiceClientDefinition
	 *            WebServiceClientDefinition
	 * @param requestType
	 *            Request Message RecordType
	 * @param responseType
	 *            Response Message RecordType
	 */
	public ServiceEndpointInterfaceInfoImpl(
			final WebServiceClientDefinition webServiceClientDefinition, final RecordType requestType, final RecordType responseType) {
		super();
		if (webServiceClientDefinition == null) {
			LOGGER.error("Argument 'webServiceClientDefinition' is null");
			throw new IllegalArgumentException();
		} else if (webServiceClientDefinition.isValid() == false) {
			LOGGER.error("Argument 'webServiceClientDefinition' is invalid");
			throw new IllegalArgumentException();
		} else if (requestType == null) {
			LOGGER.error("Argument 'requestType' is null");
			throw new IllegalArgumentException();
		} else if (responseType == null) {
			LOGGER.error("Argument 'responseType' is null");
			throw new IllegalArgumentException();
		}

		this.namespace = webServiceClientDefinition.getNamespace();
		this.wsdlAddress = webServiceClientDefinition.getWsdlAddress();
		this.serviceName = webServiceClientDefinition.getServiceName();
		this.portName = webServiceClientDefinition.getPortName();
		this.operationName = webServiceClientDefinition.getOperationName();

		if (webServiceClientDefinition.getServiceDefinition().isStandard()) {
			this.paramInfos = new ArrayList<ServiceParamInfo>();

			// header
			this.paramInfos.add(new ServiceParamInfoImpl("header", EgovWebServiceMessageHeader.TYPE, Mode.INOUT, true));

			// request body
			for (Entry<String, Type> entry : requestType.getFieldTypes().entrySet()) {
				this.paramInfos.add(new ServiceParamInfoImpl(entry.getKey(), entry.getValue(), Mode.IN, false));
			}

			// response body
			for (Entry<String, Type> entry : responseType.getFieldTypes().entrySet()) {
				this.paramInfos.add(new ServiceParamInfoImpl(entry.getKey(), entry.getValue(), Mode.OUT, false));
			}
		} else {
			Map<String, ServiceParamInfo> paramInfoMap = new HashMap<String, ServiceParamInfo>();
			if (webServiceClientDefinition.getRequestMappingInfos() != null) {
				for (Entry<String, MappingInfo> entry : webServiceClientDefinition.getRequestMappingInfos().entrySet()) {
					String fieldName = entry.getKey();
					MappingInfo mappingInfo = entry.getValue();
					Type fieldType = requestType.getFieldType(fieldName);
					ServiceParamInfoImpl paramInfo = new ServiceParamInfoImpl(fieldName, fieldType, Mode.IN, mappingInfo.isHeader());
					paramInfoMap.put(fieldName, paramInfo);
				}
			}
			if (webServiceClientDefinition.getResponseMappingInfos() != null) {
				for (Entry<String, MappingInfo> entry : webServiceClientDefinition.getResponseMappingInfos().entrySet()) {
					String fieldName = entry.getKey();
					MappingInfo mappingInfo = entry.getValue();
					Type fieldType = responseType.getFieldType(fieldName);
					ServiceParamInfoImpl paramInfo = (ServiceParamInfoImpl) paramInfoMap.get(fieldName);
					if (paramInfo != null) {
						if (paramInfo.isHeader() == mappingInfo.isHeader()) {
							if (paramInfo.getType().equals(fieldType) == false) {
							}
							paramInfo.setMode(Mode.INOUT);
						} else {
							paramInfo = null;
						}
					}
					if (paramInfo == null) {
						paramInfo = new ServiceParamInfoImpl(fieldName, fieldType, Mode.OUT, mappingInfo.isHeader());
						paramInfoMap.put(fieldName, paramInfo);
					}
				}
			}
			this.returnInfo = null;
			this.paramInfos = paramInfoMap.values();
		}
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
	 * wsdlAddress
	 * 
	 * @return the wsdlAddress
	 */
	public String getWsdlAddress() {
		return wsdlAddress;
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
	 * portName
	 * 
	 * @return the portName
	 */
	public String getPortName() {
		return portName;
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
	 * returnInfo
	 * 
	 * @return the returnInfo
	 */
	public ServiceParamInfo getReturnInfo() {
		return returnInfo;
	}

	/**
	 * paramInfos
	 * 
	 * @return the paramInfos
	 */
	public Collection<ServiceParamInfo> getParamInfos() {
		return paramInfos;
	}

}
