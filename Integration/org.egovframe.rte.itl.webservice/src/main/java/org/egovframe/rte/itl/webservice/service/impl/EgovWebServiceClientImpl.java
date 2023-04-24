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

import org.egovframe.rte.itl.integration.EgovIntegrationMessage;
import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader;
import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader.ResultCode;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessage;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessageHeader;
import org.egovframe.rte.itl.webservice.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import javax.jws.WebParam.Mode;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
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
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * 2017.02.15	장동한				시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
 * </pre>
 */
public class EgovWebServiceClientImpl implements EgovWebServiceClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovWebServiceClientImpl.class);

	/** ServiceEndpointInterfaceInfo */
	protected ServiceEndpointInterfaceInfo serviceEndpointInterfaceInfo;

	/** MessageConverter */
	protected MessageConverter messageConverter;

	/** serviceEndpointInterfaceClass */
	protected Class<?> serviceEndpointInterfaceClass;

	/** wsdl URL */
	protected URL wsdlURL;

	/** service name */
	protected QName serviceName;

	/** port name */
	protected QName portName;

	/** service */
	protected Service service;

	/** method */
	protected Method method;

	/** client */
	protected Object client;

	/** 초기화 flag */
	protected boolean initialized = false;

	/** 초기화 flag lock */
	protected Object initializedLock = new Object();

	/**
	 * Constructor
	 * 
	 * @param classLoader
	 *            EgovWebServiceClassLoader
	 * @param serviceEndpointInterfaceInfo
	 *            ServiceEndpointInterfaceInfo
	 * @param messageConverter
	 *            MessageConverter
	 * @throws IllegalArgumentException
	 *             <code>classLoader</code>,
	 *             <code>serviceEndpointInterfaceInfo</code> ,
	 *             <code>messageConverter</code> 값이 <code>null</code>인 경우
	 * @throws ClassNotFoundException
	 *             Class를 생성할 수 없는 경우
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws MalformedURLException
	 */
	public EgovWebServiceClientImpl(EgovWebServiceClassLoader classLoader,
			ServiceEndpointInterfaceInfo serviceEndpointInterfaceInfo,
			MessageConverter messageConverter) throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, MalformedURLException {
		super();

		LOGGER.debug("Create EgovWebServiceClient");

		if (classLoader == null) {
			LOGGER.error("Argument 'classLoader' is null");
			throw new IllegalArgumentException();
		} else if (serviceEndpointInterfaceInfo == null) {
			LOGGER.error("Argument 'serviceEndpointInterfaeInfo' is null");
			throw new IllegalArgumentException();
		} else if (messageConverter == null) {
			LOGGER.error("Argument 'messageConverter' is null");
			throw new IllegalArgumentException();
		}

		this.serviceEndpointInterfaceInfo = serviceEndpointInterfaceInfo;
		this.messageConverter = messageConverter;

		// ServiceEndpointInterface Class를 load한다.
		serviceEndpointInterfaceClass = classLoader.loadClass(serviceEndpointInterfaceInfo);
		LOGGER.debug("serviceEndpointInterfaceClass = {}", serviceEndpointInterfaceClass);

		// service를 생성한다.
		wsdlURL = new URL(serviceEndpointInterfaceInfo.getWsdlAddress());
		LOGGER.debug("wsdlURL = {}", wsdlURL);
		serviceName = new QName(serviceEndpointInterfaceInfo.getNamespace(), serviceEndpointInterfaceInfo.getServiceName());
		LOGGER.debug("serviceName = {}", serviceName);
		portName = new QName(serviceEndpointInterfaceInfo.getNamespace(), serviceEndpointInterfaceInfo.getPortName());
		LOGGER.debug("portName = {}", portName);

		// Method 추출
		List<Class<?>> paramClasses = new ArrayList<Class<?>>();
		for (ServiceParamInfo paramInfo : serviceEndpointInterfaceInfo.getParamInfos()) {
			if (paramInfo.getMode().equals(Mode.OUT) || paramInfo.getMode().equals(Mode.INOUT)) {
				paramClasses.add(Holder.class);
			} else {
				paramClasses.add(classLoader.loadClass(paramInfo.getType()));
			}
		}
		this.method = serviceEndpointInterfaceClass.getMethod(serviceEndpointInterfaceInfo.getOperationName(), paramClasses.toArray(new Class<?>[] {}));
		LOGGER.debug("method = {}", method);
		LOGGER.debug("Finish to creating EgovWebServiceClient");
	}

	@SuppressWarnings("unchecked")
	public EgovIntegrationMessage service(EgovIntegrationMessage requestMessage) {
		LOGGER.debug("EgovWebServiceClient service (requestMesage = {})", requestMessage);

		synchronized (initializedLock) {
			if (initialized == false) {
				LOGGER.debug("Initialize Client");
				// ServiceEndpointInterface Impl 객체 생성
				try {
					service = Service.create(wsdlURL, serviceName);
					client = service.getPort(portName, serviceEndpointInterfaceClass);
					initialized = true;
				} catch (Throwable e) {
					LOGGER.error("Cannot create web service port {}", e);
					initialized = false;
					// 초기화 실패
					return new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestMessage.getHeader()) {
						{
							setResultCode(ResultCode.FAIL_IN_INITIALIZING);
						}
					});
				}
			}
		}

		LOGGER.debug("Create Request Message");

		List<Object> params = new ArrayList<Object>();

		boolean succeed = false;
		try {
			Map<String, Object> requestBody = requestMessage.getBody();
			for (ServiceParamInfo paramInfo : serviceEndpointInterfaceInfo.getParamInfos()) {
				if (paramInfo.getMode().equals(Mode.IN) || paramInfo.getMode().equals(Mode.INOUT)) {
					Object valueObject = null;
					if (paramInfo.getType() == EgovWebServiceMessageHeader.TYPE) {
						LOGGER.debug("Insert Message Header");
						LOGGER.debug("value = {}", requestMessage.getHeader());
						valueObject = requestMessage.getHeader();
					} else {
						LOGGER.debug("Insert Param \"{}\"", paramInfo.getName());
						Object typedObject = requestBody.get(paramInfo.getName());
						valueObject = messageConverter.convertToValueObject(typedObject, paramInfo.getType());
						LOGGER.debug("value = {}", valueObject);
					}
					if (paramInfo.getMode().equals(Mode.INOUT)) {
						LOGGER.debug("Wrapping Holder");
						valueObject = new Holder<Object>(valueObject);
					}
					params.add(valueObject);
				} else {
					params.add(new Holder<Object>());
				}
			}
			succeed = true;
			
		//2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
		} catch(ClassNotFoundException e) {
			LOGGER.error("[ClassNotFoundException] Cannot Create Request Message : " + e.getMessage());
		} catch(IllegalAccessException e) {
			LOGGER.error("[IllegalAccessException] Cannot Create Request Message : " + e.getMessage());
		} catch(IllegalArgumentException e) {
		    LOGGER.error("[IllegalArgumentException] Cannot Create Request Message : " + e.getMessage());
		} catch(InstantiationException e) {
			LOGGER.error("[InstantiationException] Cannot Create Request Message : " + e.getMessage());
		} catch(NoSuchFieldException e) {
			LOGGER.error("[NoSuchFieldException] Cannot Create Request Message : " + e.getMessage());
		}

		if (succeed == false) {
			// 요청 메시시 생성 실패
			return new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestMessage.getHeader()) {
				{
					setResultCode(ResultCode.FAIL_IN_CREATING_REQUEST_MESSAGE);
				}
			});
		}

		// call method
		LOGGER.debug("Invoke method");
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
		if (succeed == false) {
			// 메소드 호출 실패
			return new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestMessage.getHeader()) {
				{
					setResultCode(ResultCode.FAIL_IN_SENDING_REQUEST);
				}
			});
		}

		// responseValueObject를 typed object로 변환한다.
		LOGGER.debug("Parse Response Message");
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
					//2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
					} catch (IllegalArgumentException e) {
						LOGGER.error("[IllegalArgumentException] Cannot parse response message : " + e.getMessage());
						succeed = false;
					} catch (ClassNotFoundException e) {
						LOGGER.error("[ClassNotFoundException] Cannot parse response message : " + e.getMessage());
						succeed = false;
					} catch (IllegalAccessException e) {
						LOGGER.error("[IllegalAccessException] Cannot parse response message : " + e.getMessage());
						succeed = false;
					} catch (NoSuchFieldException e) {
						LOGGER.error("[NoSuchFieldException] Cannot parse response message : " + e.getMessage());
						succeed = false;
					} catch (InstantiationException e) {
						LOGGER.error("[InstantiationException] Cannot parse response message : " + e.getMessage());
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
