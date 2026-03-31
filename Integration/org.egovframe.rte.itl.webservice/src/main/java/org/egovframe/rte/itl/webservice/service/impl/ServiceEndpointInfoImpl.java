/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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

import jakarta.jws.WebParam.Mode;
import org.egovframe.rte.itl.integration.type.RecordType;
import org.egovframe.rte.itl.integration.type.Type;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessageHeader;
import org.egovframe.rte.itl.webservice.data.WebServiceServerDefinition;
import org.egovframe.rte.itl.webservice.service.ServiceEndpointInfo;
import org.egovframe.rte.itl.webservice.service.ServiceParamInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

/**
 * 웹 서비스 ServiceEndpoint 정보 구현 클래스
 * <p>
 * <b>NOTE:</b> 웹 서비스 ServiceEndpoint 정보를 나타내는 class이다.
 * </p>
 *
 * @author 실행환경 개발팀 심상호
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * </pre>
 * @since 2009.06.01
 */
public class ServiceEndpointInfoImpl implements ServiceEndpointInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEndpointInfoImpl.class);

    /**
     * namespace
     */
    private String namespace;

    /**
     * address
     */
    private String address;

    /**
     * service name
     */
    private String serviceName;

    /**
     * port name
     */
    private String portName;

    /**
     * operation name
     */
    private String operationName;

    /**
     * return info
     */
    private ServiceParamInfo returnInfo;

    /**
     * param info
     */
    private Collection<ServiceParamInfo> paramInfos;

    /**
     * Constructor
     *
     * @param namespace     namespace
     * @param address       address
     * @param serviceName   service name
     * @param portName      port name
     * @param operationName operation name
     * @param returnInfo    return info
     * @param paramInfos    param info
     * @throws IllegalArgumentException Argument 값이 <code>null</code>인 경우
     */
    public ServiceEndpointInfoImpl(String namespace, String address,
                                   String serviceName, String portName, String operationName,
                                   ServiceParamInfo returnInfo, Collection<ServiceParamInfo> paramInfos) {
        super();
        if (!StringUtils.hasText(namespace)) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'namespace' has no text ({})", namespace);
            throw new IllegalArgumentException();
        } else if (!StringUtils.hasText(address)) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'address' has no text ({})", address);
            throw new IllegalArgumentException();
        } else if (!StringUtils.hasText(serviceName)) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'serviceName' has no text ({})", serviceName);
            throw new IllegalArgumentException();
        } else if (!StringUtils.hasText(portName)) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'portName' has no text ({})", portName);
            throw new IllegalArgumentException();
        } else if (!StringUtils.hasText(operationName)) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'operationName' has no text ({})", operationName);
            throw new IllegalArgumentException();
        } else if (paramInfos == null) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'paramInfos' is null");
            throw new IllegalArgumentException();
        }
        this.namespace = namespace;
        this.address = address;
        this.serviceName = serviceName;
        this.portName = portName;
        this.operationName = operationName;
        this.returnInfo = returnInfo;
        this.paramInfos = paramInfos;
    }

    /**
     * Constructor
     *
     * @param webServiceServerDefinition WebServiceServerDefinition
     * @param requestType                Request Message RecordType
     * @param responseType               Response Message RecordType
     * @throws IllegalArgumentException  Argument 값이 <code>null</code>인 경우
     */
    public ServiceEndpointInfoImpl(
            final WebServiceServerDefinition webServiceServerDefinition,
            final RecordType requestType, final RecordType responseType) {
        super();
        if (webServiceServerDefinition == null) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'webServiceServerDefinition' is null");
            throw new IllegalArgumentException();
        } else if (!webServiceServerDefinition.isValid()) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'webServiceServerDefinition' is invalid");
            throw new IllegalArgumentException();
        } else if (requestType == null) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'requestType' is null");
            throw new IllegalArgumentException();
        } else if (responseType == null) {
            LOGGER.debug("### ServiceEndpointInfoImpl Argument 'responseType' is null");
            throw new IllegalArgumentException();
        }

        this.namespace = webServiceServerDefinition.getNamespace();
        this.address = webServiceServerDefinition.getAddress();
        this.serviceName = webServiceServerDefinition.getServiceName();
        this.portName = webServiceServerDefinition.getPortName();
        this.operationName = webServiceServerDefinition.getOperationName();
        this.returnInfo = null;
        this.paramInfos = new ArrayList<>();

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
     * address
     *
     * @return the address
     */
    public String getAddress() {
        return address;
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

    public String getWsdlAddress() {
        return null;
    }

}
