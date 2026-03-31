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
import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader;
import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader.ResultCode;
import org.egovframe.rte.itl.integration.EgovIntegrationServiceProvider;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessage;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessageHeader;
import org.egovframe.rte.itl.webservice.service.MessageConverter;
import org.egovframe.rte.itl.webservice.service.ServiceBridge;
import org.egovframe.rte.itl.webservice.service.ServiceEndpointInfo;
import org.egovframe.rte.itl.webservice.service.ServiceParamInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 자동 생성된 웹 서비스 ServiceEndpoint와 EgovIntegrationServiceProvider의 인터페이스 구현 클래스
 * <p>
 * <b>NOTE:</b> 자동 생성된 웹 서비스 ServiceEndpoint와 EgovIntegrationServiceProvider를
 * 연결해주는 ServiceBridge class이다. </p>
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
 */
public class ServiceBridgeImpl implements ServiceBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBridgeImpl.class);

    /**
     * 서비스 제공 모듈
     */
    private EgovIntegrationServiceProvider provider;

    /**
     * ServiceEndpointInfo
     */
    private ServiceEndpointInfo serviceEndpointInfo;

    /**
     * MessageConverter
     */
    private MessageConverter messageConverter;

    /**
     * Constructor
     *
     * @param provider            서비스 제공 모듈
     * @param serviceEndpointInfo ServiceEndpointInfo
     * @param messageConverter    MessageConverter
     * @throws IllegalArgumentException <code>provider</code>, <code>serviceEndpointInfo</code>,
     *                                  <code>messageConverter</code> 값이 <code>null</code>인 경우
     */
    public ServiceBridgeImpl(EgovIntegrationServiceProvider provider, ServiceEndpointInfo serviceEndpointInfo, MessageConverter messageConverter) {
        super();
        if (provider == null) {
            LOGGER.debug("### ServiceBridgeImpl Argument 'provider' is null");
            throw new IllegalArgumentException();
        } else if (serviceEndpointInfo == null) {
            LOGGER.debug("### ServiceBridgeImpl Argument 'serviceEndpointInfo' is null");
            throw new IllegalArgumentException();
        } else if (messageConverter == null) {
            LOGGER.debug("### ServiceBridgeImpl Argument 'messageConverter' is null");
            throw new IllegalArgumentException();
        }
        this.provider = provider;
        this.serviceEndpointInfo = serviceEndpointInfo;
        this.messageConverter = messageConverter;
    }

    @SuppressWarnings("unchecked")
    public Object doService(Map<String, Object> params) {
        LOGGER.debug("### ServiceBridgeImpl doService() Start ");
        // requestMessage를 parsing, 최대한 가능한 부분을 parsing한다.
        LOGGER.debug("### ServiceBridgeImpl doService() : Parse Request Message");
        EgovIntegrationMessageHeader requestHeader = null;
        Map<String, Object> requestBody = new HashMap<String, Object>();
        boolean succeed = true;
        for (ServiceParamInfo paramInfo : serviceEndpointInfo.getParamInfos()) {
            if (paramInfo.getMode().equals(Mode.IN) || paramInfo.getMode().equals(Mode.INOUT)) {
                Object valueObject = params.get(paramInfo.getName());
                if (paramInfo.getMode().equals(Mode.INOUT)) {
                    valueObject = ((Holder<Object>) valueObject).value;
                }
                if (paramInfo.getType() == EgovWebServiceMessageHeader.TYPE) {
                    requestHeader = (EgovWebServiceMessageHeader) valueObject;
                } else {
                    try {
                        Object typedObject = messageConverter.convertToTypedObject(valueObject, paramInfo.getType());
                        requestBody.put(paramInfo.getName(), typedObject);
                        //2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
                    } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchFieldException e) {
                        LOGGER.debug("[{}] ServiceBridgeImpl doService() Cannot Create Request Message : {}", e.getClass().getName(), e.getMessage());
                        succeed = false;
                    }
                }
            }
        }

        // 현재 시각
        final Calendar now = Calendar.getInstance();

        // 응답 메시지
        EgovWebServiceMessage responseMessage = null;

        if (!succeed) {
            // 요청 메시지 parsing 실패
            LOGGER.debug("### ServiceBridgeImpl doService() : Fail to parse request message");
            EgovWebServiceMessageHeader responseHeader = null;
            if (requestHeader == null) {
                responseHeader = new EgovWebServiceMessageHeader();
            } else {
                responseHeader = new EgovWebServiceMessageHeader(requestHeader);
            }
            responseHeader.setRequestReceiveTime(now);
            responseHeader.setResultCode(ResultCode.FAIL_IN_PARSING_REQUEST_MESSAGE);
            responseMessage = new EgovWebServiceMessage(responseHeader);
        } else if (requestHeader == null) {
            // 요청 메시지 헤더가 없음
            LOGGER.debug("### ServiceBridgeImpl doService() : No MessageHeader in request message");
            responseMessage = new EgovWebServiceMessage(new EgovWebServiceMessageHeader() {
                {
                    setRequestReceiveTime(now);
                    setResultCode(ResultCode.NO_MESSAGE_HEADER_IN_REQUEST);
                }
            });
        } else {
            // 요청 수신 시각 설정
            requestHeader.setRequestReceiveTime(Calendar.getInstance());
            // 요청 메시지 생성
            EgovWebServiceMessage requestMessage = new EgovWebServiceMessage(requestHeader, requestBody);
            // empty 응답 메시지 생성
            responseMessage = new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestHeader));
            succeed = true;

            // 2026.02.28 KISA 보안취약점 조치
            try {
                provider.service(requestMessage, responseMessage);
            } catch (RuntimeException e) {
                LOGGER.debug("[{}] ServiceBridgeImpl doService() : Fail to call provider's service (RuntimeException) : {}", e.getClass().getSimpleName(), e.getMessage());
                responseMessage = new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestHeader) {
                    { setResultCode(ResultCode.FAIL_TO_CALL_SERVICE_PROVIDER); }
                });
                succeed = false;
            } catch (Exception e) {
                LOGGER.debug("[{}] ServiceBridgeImpl doService() : Fail to call provider's service : {}", e.getClass().getSimpleName(), e.getMessage());
                responseMessage = new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestHeader) {
                    { setResultCode(ResultCode.FAIL_TO_CALL_SERVICE_PROVIDER); }
                });
                succeed = false;
            }
        }

        LOGGER.debug("### ServiceBridgeImpl doService() : Create Response Message");

        if (succeed) {
            // 2026.02.28 KISA 보안취약점 조치
            try {
                Map<String, Object> responseBody = responseMessage.getBody();
                for (ServiceParamInfo paramInfo : serviceEndpointInfo.getParamInfos()) {
                    if (paramInfo.getMode().equals(Mode.OUT) || paramInfo.getMode().equals(Mode.INOUT)) {
                        Object valueObject = null;
                        if (paramInfo.getType() == EgovWebServiceMessageHeader.TYPE) {
                            valueObject = responseMessage.getHeader();
                        } else {
                            Object typedObject = responseBody.get(paramInfo.getName());
                            valueObject = messageConverter.convertToValueObject(typedObject, paramInfo.getType());
                        }
                        ((Holder<Object>) params.get(paramInfo.getName())).value = valueObject;
                    }
                }
                responseMessage.getHeader().setResponseSendTime(Calendar.getInstance());
                return null;
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchFieldException e) {
                LOGGER.debug("[{}] ServiceBridgeImpl doService() : Cannot create response message (InstantiationException) : {}", e.getClass().getSimpleName(), e.getMessage());
                responseMessage = new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestHeader) {
                    { setResultCode(ResultCode.FAIL_IN_CREATING_RESPONSE_MESSAGE); }
                });
                succeed = false;
            } catch (Exception e) {
                LOGGER.debug("[{}] ServiceBridgeImpl doService() : Cannot create response message : {}", e.getClass().getSimpleName(), e.getMessage());
                responseMessage = new EgovWebServiceMessage(new EgovWebServiceMessageHeader(requestHeader) {
                    { setResultCode(ResultCode.FAIL_IN_CREATING_RESPONSE_MESSAGE); }
                });
                succeed = false;
            }
        }

        // 전체 실패
        // 헤더부만 찾아서 헤더만 넘긴다.
        ServiceParamInfo headerParamInfo = null;
        for (ServiceParamInfo paramInfo : serviceEndpointInfo.getParamInfos()) {
            if (paramInfo.getType() == EgovWebServiceMessageHeader.TYPE) {
                headerParamInfo = paramInfo;
            }
        }

        if (headerParamInfo == null) {
            // 발생하면 안됨
            LOGGER.debug("### ServiceBridgeImpl doService() : No Header ParamInfo");
        } else if (headerParamInfo.getMode().equals(Mode.IN)) {
            // 발생하면 안됨
            LOGGER.debug("### ServiceBridgeImpl doService() : Header ParamInfo's Mode must be IN or INOUT");
        } else {
            ((Holder<Object>) params.get(headerParamInfo.getName())).value = responseMessage.getHeader();
            responseMessage.getHeader().setResponseSendTime(Calendar.getInstance());
        }

        return null;
    }

}
