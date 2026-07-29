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
import jakarta.xml.ws.Holder;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.egovframe.rte.itl.integration.EgovIntegrationMessage;
import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader;
import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader.ResultCode;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessage;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessageHeader;
import org.egovframe.rte.itl.webservice.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 웹서비스 Client로 실제 웹서비스를 호출하는 구현 클래스
 * <p>
 * <b>NOTE:</b> 웹서비스 Client로 실제 웹서비스를 호출하는 class이다. </p>
 *
 * @author 실행환경 개발팀 심상호
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * 2017.02.15	장동한				시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
 * </pre>
 * @since 2009.06.01
 *
 * <p><b>보안 주의:</b> {@code wsdlAddress}(WSDL 조회 URL)는 신뢰할 수 있는 연계 설정에서만 지정해야
 * 한다 — 이 값이 외부 입력으로 오염되면 SSRF로 이어질 수 있다. 또한 WSDL/XSD 파싱 시 XXE 방어는 이
 * 클래스가 별도로 강화하는 것이 아니라 {@link jakarta.xml.ws.Service#create}가 내부적으로 사용하는
 * CXF/JAX-WS 런타임의 기본 XML 파서 보안 설정에 의존한다.</p>
 */
public class EgovWebServiceClientImpl implements EgovWebServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovWebServiceClientImpl.class);

    /** 연결 타임아웃 기본값(ms) — 연계 대상이 무응답이어도 호출 스레드가 무한정 블로킹되지 않도록 함 */
    private static final long DEFAULT_CONNECTION_TIMEOUT_MS = 5000L;
    /** 응답 수신 타임아웃 기본값(ms) */
    private static final long DEFAULT_RECEIVE_TIMEOUT_MS = 30000L;

    /**
     * 연결/응답 타임아웃(ms). 소비 앱이 {@link #setConnectionTimeout(long)}/{@link #setReceiveTimeout(long)}로
     * 재정의하지 않으면 기본값이 적용된다.
     */
    private long connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MS;
    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT_MS;

    /**
     * ServiceEndpointInterfaceInfo
     */
    protected ServiceEndpointInterfaceInfo serviceEndpointInterfaceInfo;

    /**
     * MessageConverter
     */
    protected MessageConverter messageConverter;

    /**
     * serviceEndpointInterfaceClass
     */
    protected Class<?> serviceEndpointInterfaceClass;

    /**
     * wsdl URL
     */
    protected URL wsdlURL;

    /**
     * service name
     */
    protected QName serviceName;

    /**
     * port name
     */
    protected QName portName;

    /**
     * service
     */
    protected Service service;

    /**
     * method
     */
    protected Method method;

    /**
     * client
     */
    protected Object client;

    /**
     * 초기화 flag
     */
    protected boolean initialized = false;

    /**
     * 초기화 flag lock
     */
    protected final Object initializedLock = new Object();

    /**
     * Constructor
     *
     * @param classLoader                  EgovWebServiceClassLoader
     * @param serviceEndpointInterfaceInfo ServiceEndpointInterfaceInfo
     * @param messageConverter             MessageConverter
     * @throws IllegalArgumentException <code>classLoader</code>,
     *                                  <code>serviceEndpointInterfaceInfo</code> ,
     *                                  <code>messageConverter</code> 값이 <code>null</code>인 경우
     * @throws ClassNotFoundException   Class를 생성할 수 없는 경우
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws MalformedURLException
     */
    public EgovWebServiceClientImpl(EgovWebServiceClassLoader classLoader,
                                    ServiceEndpointInterfaceInfo serviceEndpointInterfaceInfo,
                                    MessageConverter messageConverter) throws ClassNotFoundException,
            SecurityException, NoSuchMethodException, MalformedURLException {
        super();

        LOGGER.debug("### EgovWebServiceClientImpl : Create EgovWebServiceClient");

        if (classLoader == null) {
            LOGGER.debug("### EgovWebServiceClientImpl Argument 'classLoader' is null");
            throw new IllegalArgumentException();
        } else if (serviceEndpointInterfaceInfo == null) {
            LOGGER.debug("### EgovWebServiceClientImpl Argument 'serviceEndpointInterfaeInfo' is null");
            throw new IllegalArgumentException();
        } else if (messageConverter == null) {
            LOGGER.debug("### EgovWebServiceClientImpl Argument 'messageConverter' is null");
            throw new IllegalArgumentException();
        }

        this.serviceEndpointInterfaceInfo = serviceEndpointInterfaceInfo;
        this.messageConverter = messageConverter;

        // ServiceEndpointInterface Class를 load한다.
        serviceEndpointInterfaceClass = classLoader.loadClass(serviceEndpointInterfaceInfo);
        LOGGER.debug("### EgovWebServiceClientImpl serviceEndpointInterfaceClass = {}", serviceEndpointInterfaceClass);

        // service를 생성한다.
        wsdlURL = new URL(serviceEndpointInterfaceInfo.getWsdlAddress());
        LOGGER.debug("### EgovWebServiceClientImpl wsdlURL = {}", wsdlURL);
        serviceName = new QName(serviceEndpointInterfaceInfo.getNamespace(), serviceEndpointInterfaceInfo.getServiceName());
        LOGGER.debug("### EgovWebServiceClientImpl serviceName = {}", serviceName);
        portName = new QName(serviceEndpointInterfaceInfo.getNamespace(), serviceEndpointInterfaceInfo.getPortName());
        LOGGER.debug("### EgovWebServiceClientImpl : portName = {}", portName);

        // Method 추출
        List<Class<?>> paramClasses = new ArrayList<Class<?>>();
        for (ServiceParamInfo paramInfo : serviceEndpointInterfaceInfo.getParamInfos()) {
            if (paramInfo.getMode().equals(Mode.OUT) || paramInfo.getMode().equals(Mode.INOUT)) {
                paramClasses.add(Holder.class);
            } else {
                paramClasses.add(classLoader.loadClass(paramInfo.getType()));
            }
        }
        this.method = serviceEndpointInterfaceClass.getMethod(serviceEndpointInterfaceInfo.getOperationName(), paramClasses.toArray(new Class<?>[]{}));
        LOGGER.debug("### EgovWebServiceClientImpl method = {}", method);
        LOGGER.debug("### EgovWebServiceClientImpl Finish to creating EgovWebServiceClient");
    }

    /**
     * 연결 타임아웃(ms)을 재정의한다. 기본값은 {@value #DEFAULT_CONNECTION_TIMEOUT_MS}ms.
     */
    public void setConnectionTimeout(long connectionTimeoutMillis) {
        this.connectionTimeout = connectionTimeoutMillis;
    }

    /**
     * 응답 수신 타임아웃(ms)을 재정의한다. 기본값은 {@value #DEFAULT_RECEIVE_TIMEOUT_MS}ms.
     */
    public void setReceiveTimeout(long receiveTimeoutMillis) {
        this.receiveTimeout = receiveTimeoutMillis;
    }

    /**
     * 연계 대상 서비스가 응답을 지연시키더라도 호출 스레드/소켓이 무한정 블로킹되지 않도록
     * CXF HTTPConduit에 연결/응답 타임아웃을 적용한다. CXF가 아닌 JAX-WS 런타임이 사용되어
     * HTTPConduit로 캐스팅할 수 없는 경우 조용히 무시한다(타임아웃 미적용 상태로 남을 수 있으므로
     * 그 경우 호출측에서 별도 타임아웃 전략이 필요하다).
     */
    private void applyDefaultTimeouts(Object clientPort) {
        try {
            Client cxfClient = ClientProxy.getClient(clientPort);
            if (cxfClient.getConduit() instanceof HTTPConduit) {
                HTTPConduit httpConduit = (HTTPConduit) cxfClient.getConduit();
                HTTPClientPolicy policy = httpConduit.getClient();
                if (policy == null) {
                    policy = new HTTPClientPolicy();
                }
                policy.setConnectionTimeout(connectionTimeout);
                policy.setReceiveTimeout(receiveTimeout);
                httpConduit.setClient(policy);
                LOGGER.debug("### EgovWebServiceClientImpl applyDefaultTimeouts() connectionTimeout={}, receiveTimeout={}",
                        connectionTimeout, receiveTimeout);
            }
        } catch (RuntimeException e) {
            LOGGER.debug("[{}] EgovWebServiceClientImpl applyDefaultTimeouts() Could not apply HTTP timeout policy: {}",
                    e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public EgovIntegrationMessage service(EgovIntegrationMessage requestMessage) {
        LOGGER.debug("### EgovWebServiceClientImpl service() requestMesage : {}", requestMessage);

        synchronized (initializedLock) {
            if (!initialized) {
                LOGGER.debug("### EgovWebServiceClientImpl service() Initialize Client");
                // ServiceEndpointInterface Impl 객체 생성
                // 2026.02.28 KISA 보안취약점 조치
                try {
                    service = Service.create(wsdlURL, serviceName);
                    client = service.getPort(portName, serviceEndpointInterfaceClass);
                    applyDefaultTimeouts(client);
                    initialized = true;
                } catch (WebServiceException e) {
                    LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot create web service port (WebServiceException) : {}", e.getClass().getSimpleName(), e.getMessage());
                    initialized = false;
                    return new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestMessage.getHeader()) {
                        { setResultCode(ResultCode.FAIL_IN_INITIALIZING); }
                    });
                } catch (Exception e) {
                    LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot create web service port : {}", e.getClass().getSimpleName(), e.getMessage());
                    initialized = false;
                    return new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestMessage.getHeader()) {
                        { setResultCode(ResultCode.FAIL_IN_INITIALIZING); }
                    });
                }
            }
        }

        LOGGER.debug("### EgovWebServiceClientImpl service() Create Request Message");

        List<Object> params = new ArrayList<Object>();

        boolean succeed = false;
        try {
            Map<String, Object> requestBody = requestMessage.getBody();
            for (ServiceParamInfo paramInfo : serviceEndpointInterfaceInfo.getParamInfos()) {
                if (paramInfo.getMode().equals(Mode.IN) || paramInfo.getMode().equals(Mode.INOUT)) {
                    Object valueObject = null;
                    if (paramInfo.getType() == EgovWebServiceMessageHeader.TYPE) {
                        LOGGER.debug("### EgovWebServiceClientImpl service() Insert Message Header");
                        LOGGER.debug("### EgovWebServiceClientImpl service() value : {}", requestMessage.getHeader());
                        valueObject = requestMessage.getHeader();
                    } else {
                        LOGGER.debug("### EgovWebServiceClientImpl service() Insert Param : {}", paramInfo.getName());
                        Object typedObject = requestBody.get(paramInfo.getName());
                        valueObject = messageConverter.convertToValueObject(typedObject, paramInfo.getType());
                        LOGGER.debug("### EgovWebServiceClientImpl service() value : {}", valueObject);
                    }
                    if (paramInfo.getMode().equals(Mode.INOUT)) {
                        LOGGER.debug("### EgovWebServiceClientImpl service() Wrapping Holder");
                        valueObject = new Holder<Object>(valueObject);
                    }
                    params.add(valueObject);
                } else {
                    params.add(new Holder<Object>());
                }
            }
            succeed = true;

        } catch (ClassNotFoundException e) {
            LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot Create Request Message (ClassNotFoundException) : {}", e.getClass().getSimpleName(), e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot Create Request Message (IllegalAccessException) : {}", e.getClass().getSimpleName(), e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot Create Request Message (IllegalArgumentException) : {}", e.getClass().getSimpleName(), e.getMessage());
        } catch (InstantiationException e) {
            LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot Create Request Message (InstantiationException) : {}", e.getClass().getSimpleName(), e.getMessage());
        } catch (NoSuchFieldException e) {
            LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot Create Request Message (NoSuchFieldException) : {}", e.getClass().getSimpleName(), e.getMessage());
        }

        if (!succeed) {
            // 요청 메시시 생성 실패
            return new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestMessage.getHeader()) {
                {
                    setResultCode(ResultCode.FAIL_IN_CREATING_REQUEST_MESSAGE);
                }
            });
        }

        // call method
        LOGGER.debug("### EgovWebServiceClientImpl service() Invoke method");
        Object[] paramArray = params.toArray();
        succeed = false;
        try {
            method.invoke(client, paramArray);
            succeed = true;
        } catch (IllegalAccessException e) {
            ReflectionUtils.handleReflectionException(e);
        } catch (InvocationTargetException e) {
            ReflectionUtils.handleReflectionException(e);
        }
        if (!succeed) {
            // 메소드 호출 실패
            return new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestMessage.getHeader()) {
                {
                    setResultCode(ResultCode.FAIL_IN_SENDING_REQUEST);
                }
            });
        }

        // responseValueObject를 typed object로 변환한다.
        LOGGER.debug("### EgovWebServiceClientImpl service() Parse Response Message");
        EgovIntegrationMessageHeader responseHeader = null;
        succeed = true;

        // response parsing은 최대한 가능한 만큼 수행한다.
        Map<String, Object> responseBody = new HashMap<String, Object>();
        int i = 0;
        for (ServiceParamInfo paramInfo : serviceEndpointInterfaceInfo.getParamInfos()) {
            if (paramInfo.getMode().equals(Mode.OUT) || paramInfo.getMode().equals(Mode.INOUT)) {
                Object valueObject = ((Holder<Object>) paramArray[i]).value;
                if (paramInfo.getType() == EgovWebServiceMessageHeader.TYPE) {
                    responseHeader = (EgovWebServiceMessageHeader) valueObject;
                } else {
                    try {
                        Object typedObject = messageConverter.convertToTypedObject(valueObject, paramInfo.getType());
                        responseBody.put(paramInfo.getName(), typedObject);
                    } catch (IllegalArgumentException e) {
                        LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot parse response message (IllegalArgumentException) : {}", e.getClass().getSimpleName(), e.getMessage());
                        succeed = false;
                    } catch (ClassNotFoundException e) {
                        LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot parse response message (ClassNotFoundException) : {}", e.getClass().getSimpleName(), e.getMessage());
                        succeed = false;
                    } catch (IllegalAccessException e) {
                        LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot parse response message (IllegalAccessException) : {}", e.getClass().getSimpleName(), e.getMessage());
                        succeed = false;
                    } catch (NoSuchFieldException e) {
                        LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot parse response message (NoSuchFieldException) : {}", e.getClass().getSimpleName(), e.getMessage());
                        succeed = false;
                    } catch (InstantiationException e) {
                        LOGGER.debug("[{}] EgovWebServiceClientImpl service() Cannot parse response message (InstantiationException) : {}", e.getClass().getSimpleName(), e.getMessage());
                        succeed = false;
                    }
                }
            }
            i++;
        }

        if (succeed) {
            // 응답 메시지 헤더가 없는 경우, requestHeader를 이용하여 생성
            if (responseHeader == null) {
                responseHeader = new EgovWebServiceMessageHeader(requestMessage.getHeader());
            }
            return new EgovWebServiceMessage(responseHeader, responseBody);
        } else {
            // 응답 메시지 parsing 실패
            if (responseHeader == null) {
                responseHeader = new EgovWebServiceMessageHeader(requestMessage.getHeader());
            }
            responseHeader.setResultCode(ResultCode.FAIL_IN_PARSING_RESPONSE_MESSAGE);
            return new EgovWebServiceMessage(responseHeader, responseBody);
        }
    }

}
