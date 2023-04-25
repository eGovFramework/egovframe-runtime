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
package org.egovframe.rte.itl.webservice;

import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader;
import org.egovframe.rte.itl.integration.type.RecordType;
import org.egovframe.rte.itl.integration.type.Type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Calendar;

/**
 * 전자정부 웹 서비스 메시지 헤더 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 웹 서비스 메시지 헤더 class이다. 
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
@XmlAccessorType(XmlAccessType.FIELD)
public class EgovWebServiceMessageHeader implements EgovIntegrationMessageHeader {

	public static final Type TYPE = new RecordType( "__egovWebServiceMessageHeader__", "EgovWebServiceMessageHeader");

	/** 연계 ID */
	protected String integrationId;

	/** 제공 기관 ID */
	protected String providerOrganizationId;

	/** 제공 시스템 ID */
	protected String providerSystemId;

	/** 제공 서비스 ID */
	protected String providerServiceId;

	/** 요청 기관 ID */
	protected String consumerOrganizationId;

	/** 요청 시스템 ID */
	protected String consumerSystemId;

	/** 요청 송신 시각 */
	protected Calendar requestSendTime;

	/** 요청 수신 시각 */
	protected Calendar requestReceiveTime;

	/** 응답 송신 시각 */
	protected Calendar responseSendTime;

	/** 응답 수신 시각 */
	protected Calendar responseReceiveTime;

	/** 결과 코드 */
	protected String resultCode;

	/** 결과 메시지 */
	protected String resultMessage;

	/**
	 * Default Constructor
	 */
	public EgovWebServiceMessageHeader() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param header
	 *            원본 header
	 */
	public EgovWebServiceMessageHeader(EgovIntegrationMessageHeader header) {
		super();
		setIntegrationId(header.getIntegrationId());
		setProviderOrganizationId(header.getProviderOrganizationId());
		setProviderSystemId(header.getProviderSystemId());
		setProviderServiceId(header.getProviderServiceId());
		setConsumerOrganizationId(header.getConsumerOrganizationId());
		setConsumerSystemId(header.getConsumerSystemId());
		setRequestSendTime(header.getRequestSendTime());
		setRequestReceiveTime(header.getRequestReceiveTime());
		setResponseSendTime(header.getResponseSendTime());
		setResponseReceiveTime(header.getResponseReceiveTime());
		setResultCode(header.getResultCode());
		setResultMessage(header.getResultMessage());
	}

	/**
	 * Constructor
	 * 
	 * @param integrationId
	 *            연계 ID
	 * @param providerOrganizationId
	 *            제공 기관 ID
	 * @param providerSystemId
	 *            제공 시스템 ID
	 * @param providerServiceId
	 *            제공 서비스 ID
	 * @param consumerOrganizationId
	 *            요청 기관 ID
	 * @param consumerSystemId
	 *            요청 시스템 ID
	 * @param requestSendTime
	 *            요청 송신 시각
	 * @param requestReceiveTime
	 *            요청 수신 시각
	 * @param responseSendTime
	 *            응답 송신 시각
	 * @param responseReceiveTime
	 *            응답 수신 시각
	 * @param resultCode
	 *            결과 코드
	 * @param resultMessage
	 *            결과 메시지
	 */
	public EgovWebServiceMessageHeader(String integrationId,
			String providerOrganizationId, String providerSystemId,
			String providerServiceId, String consumerOrganizationId,
			String consumerSystemId, Calendar requestSendTime,
			Calendar requestReceiveTime, Calendar responseSendTime,
			Calendar responseReceiveTime, ResultCode resultCode,
			String resultMessage) {
		setIntegrationId(integrationId);
		setProviderOrganizationId(providerOrganizationId);
		setProviderSystemId(providerSystemId);
		setProviderServiceId(providerServiceId);
		setConsumerOrganizationId(consumerOrganizationId);
		setConsumerSystemId(consumerSystemId);
		setRequestSendTime(requestSendTime);
		setRequestReceiveTime(requestReceiveTime);
		setResponseSendTime(responseSendTime);
		setResponseReceiveTime(responseReceiveTime);
		setResultCode(resultCode);
		setResultMessage(resultMessage);
	}

	/** 
	 * integrationId
	 * 
	 * @return integrationId
	 */
	public String getIntegrationId() {
		return integrationId;
	}

	/**
	 * integrationId
	 * 
	 * @param integrationId
	 */	
	public void setIntegrationId(String integrationId) {
		this.integrationId = integrationId;
	}

	/** 
	 * providerOrganizationId
	 * 
	 * @return providerOrganizationId
	 */
	public String getProviderOrganizationId() {
		return providerOrganizationId;
	}

	/**
	 * providerOrganizationId
	 * 
	 * @param providerOrganizationId
	 */
	public void setProviderOrganizationId(String providerOrganizationId) {
		this.providerOrganizationId = providerOrganizationId;
	}
	
    /** 
     * providerSystemId 
     * 
     * @return providerSystemId
     */
	public String getProviderSystemId() {
		return providerSystemId;
	}

	/** 
	 * providerSystemId
	 * 
	 * @param providerSystemId
	 */
	public void setProviderSystemId(String providerSystemId) {
		this.providerSystemId = providerSystemId;
	}

	/** 
	 * providerServiceId
	 * 
	 * @return providerServiceId
	 */
	public String getProviderServiceId() {
		return providerServiceId;
	}

	/**
	 * providerServiceId
	 * 
	 * @param providerServiceId
	 */
	public void setProviderServiceId(String providerServiceId) {
		this.providerServiceId = providerServiceId;
	}

	/**
	 * consumerOrganizationId
	 * 
	 * @return consumerOrganizationId
	 */
	public String getConsumerOrganizationId() {
		return consumerOrganizationId;
	}

	/**
	 * consumerOrganizationId
	 * 
	 * @param consumerOrganizationId
	 */
	public void setConsumerOrganizationId(String consumerOrganizationId) {
		this.consumerOrganizationId = consumerOrganizationId;
	}

	/**
	 * consumerSystemId
	 * 
	 * @return consumerSystemId
	 */
	public String getConsumerSystemId() {
		return consumerSystemId;
	}

	/**
	 * consumerSystemId
	 * 
	 * @param consumerSystemId
	 */
	public void setConsumerSystemId(String consumerSystemId) {
		this.consumerSystemId = consumerSystemId;
	}
	/**
	 * requestSendTime
	 * 
	 * @return requestSendTime
	 */
	public Calendar getRequestSendTime() {
		return requestSendTime;
	}

	/**
	 * requestSendTime
	 * 
	 * @param requestSendTime
	 */
	public void setRequestSendTime(Calendar requestSendTime) {
		this.requestSendTime = requestSendTime;
	}

	/**
	 * requestReceiveTime
	 * 
	 * @return requestReceiveTime
	 */
	public Calendar getRequestReceiveTime() {
		return requestReceiveTime;
	}

	/**
	 * requestReceiveTime
	 * 
	 * @param requestReceiveTime
	 */
	public void setRequestReceiveTime(Calendar requestReceiveTime) {
		this.requestReceiveTime = requestReceiveTime;
	}

	/**
	 * responseSendTime
	 * 
	 * @return responseSendTime
	 */
	public Calendar getResponseSendTime() {
		return responseSendTime;
	}

	/**
	 * responseSendTime
	 * 
	 * @param responseSendTime
	 */
	public void setResponseSendTime(Calendar responseSendTime) {
		this.responseSendTime = responseSendTime;
	}

	/**
	 * responseReceiveTime
	 * 
	 * @return responseReceiveTime
	 */
	public Calendar getResponseReceiveTime() {
		return responseReceiveTime;
	}

	/**
	 * responseReceiveTime
	 * 
	 * @param responseReceiveTime
	 */
	public void setResponseReceiveTime(Calendar responseReceiveTime) {
		this.responseReceiveTime = responseReceiveTime;
	}

	/**
	 * resultCode
	 * 
	 * @return resultCode
	 */
	public ResultCode getResultCode() {
		return ResultCode.getCode(resultCode);
	}

	/**
	 * resultCode
	 * 
	 * @param resultCode
	 */
	public void setResultCode(ResultCode resultCode) {
		this.resultCode = resultCode.getValue();
	}

	/**
	 * resultMessage
	 * 
	 * @return resultMessage
	 */
	public String getResultMessage() {
		return resultMessage;
	}

	/**
	 * resultMessage
	 * 
	 * @param resultMessage
	 */
	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

}
