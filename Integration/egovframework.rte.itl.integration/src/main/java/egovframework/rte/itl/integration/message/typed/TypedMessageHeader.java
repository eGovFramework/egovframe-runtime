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
package egovframework.rte.itl.integration.message.typed;

import java.util.Calendar;
import java.util.HashMap;

import egovframework.rte.itl.integration.EgovIntegrationMessageHeader;
import egovframework.rte.itl.integration.type.PrimitiveType;
import egovframework.rte.itl.integration.type.RecordType;
import egovframework.rte.itl.integration.type.Type;

/**
 * 전자정부 연계 서비스의 표준 MessageHeader 구현 클래스.
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 MessageHeader interface를 구현한 MessageHeader
 * class이다.
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
public final class TypedMessageHeader extends TypedMap implements EgovIntegrationMessageHeader {
	/** 연계 Id */
	public static final String INTEGRATION_ID = "__integrationId__";

	/** 제공 기관 Id */
	public static final String PROVIDER_ORGANIZATION_ID = "__providerOrganizationId__";

	/** 제공 시스템 Id */
	public static final String PROVIDER_SYSTEM_ID = "__providerSystemId__";

	/** 제공 서비스 Id */
	public static final String PROVIDER_SERVICE_ID = "__providerServiceId__";

	/** 요청 기관 Id */
	public static final String CONSUMER_ORGANIZATION_ID = "__consumerOrganizationId__";

	/** 요청 시스템 Id */
	public static final String CONSUMER_SYSTEM_ID = "_consumerSystemId__";

	/** 요청 송신 시각 */
	public static final String REQUEST_SEND_TIME = "__requestSendTime__";

	/** 요청 수신 시각 */
	public static final String REQUEST_RECEIVE_TIME = "__requestReceiveTime__";

	/** 응답 송신 시각 */
	public static final String RESPONSE_SEND_TIME = "__responseSendTime__";

	/** 응답 수신 시각 */
	public static final String RESPONSE_RECEIVE_TIME = "__responseReceiveTime__";

	/** 결과 Code */
	public static final String RESULT_CODE = "__resultCode__";

	/** 결과 메시지 */
	public static final String RESULT_MESSAGE = "__resultMessage__";

	/** TYPE_ID */
	public static final String TYPE_ID = "__message_header_type__";

	/** TYPE_NAME */
	public static final String TYPE_NAME = "$$Egov$$TypedMessageHeaderType";

	/** TYPE */
	public static final RecordType TYPE = new RecordType(TYPE_ID, TYPE_NAME, new HashMap<String, Type>() {
		/**
		 * serialVersion UID
		 */
		private static final long serialVersionUID = -2025491045461925409L;

		{
			put(INTEGRATION_ID, PrimitiveType.STRING);
			put(PROVIDER_ORGANIZATION_ID, PrimitiveType.STRING);
			put(PROVIDER_SYSTEM_ID, PrimitiveType.STRING);
			put(PROVIDER_SERVICE_ID, PrimitiveType.STRING);
			put(CONSUMER_ORGANIZATION_ID, PrimitiveType.STRING);
			put(CONSUMER_SYSTEM_ID, PrimitiveType.STRING);
			put(REQUEST_SEND_TIME, PrimitiveType.CALENDAR);
			put(REQUEST_RECEIVE_TIME, PrimitiveType.CALENDAR);
			put(RESPONSE_SEND_TIME, PrimitiveType.CALENDAR);
			put(RESPONSE_SEND_TIME, PrimitiveType.CALENDAR);
			put(RESULT_CODE, PrimitiveType.STRING);
			put(RESULT_MESSAGE, PrimitiveType.STRING);
		}
	});

	/**
	 * Default Constructor
	 */
	public TypedMessageHeader() {
		super(TYPE);
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
	public TypedMessageHeader(String integrationId, String providerOrganizationId, String providerSystemId, String providerServiceId, String consumerOrganizationId,
			String consumerSystemId, Calendar requestSendTime, Calendar requestReceiveTime, Calendar responseSendTime, Calendar responseReceiveTime, ResultCode resultCode,
			String resultMessage) {
		super(TYPE);
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

	public String getConsumerOrganizationId() {
		return (String) super.get(CONSUMER_ORGANIZATION_ID);
	}

	public String getConsumerSystemId() {
		return (String) super.get(CONSUMER_SYSTEM_ID);
	}

	public String getIntegrationId() {
		return (String) super.get(INTEGRATION_ID);
	}

	public String getProviderOrganizationId() {
		return (String) super.get(PROVIDER_ORGANIZATION_ID);
	}

	public String getProviderServiceId() {
		return (String) super.get(PROVIDER_SERVICE_ID);
	}

	public String getProviderSystemId() {
		return (String) super.get(PROVIDER_SYSTEM_ID);
	}

	public Calendar getRequestReceiveTime() {
		return (Calendar) super.get(REQUEST_RECEIVE_TIME);
	}

	public Calendar getRequestSendTime() {
		return (Calendar) super.get(REQUEST_SEND_TIME);
	}

	public Calendar getResponseReceiveTime() {
		return (Calendar) super.get(RESPONSE_RECEIVE_TIME);
	}

	public Calendar getResponseSendTime() {
		return (Calendar) super.get(RESPONSE_SEND_TIME);
	}

	public ResultCode getResultCode() {
		return ResultCode.getCode((String) super.get(RESULT_CODE));
	}

	public String getResultMessage() {
		return (String) super.get(RESULT_MESSAGE);
	}

	public void setConsumerOrganizationId(String consumerOrganizationId) {
		super.put(CONSUMER_ORGANIZATION_ID, consumerOrganizationId);
	}

	public void setConsumerSystemId(String consumerSystemId) {
		super.put(CONSUMER_SYSTEM_ID, consumerSystemId);
	}

	public void setIntegrationId(String integrationId) {
		super.put(INTEGRATION_ID, integrationId);
	}

	public void setProviderOrganizationId(String providerOrganizationId) {
		super.put(PROVIDER_ORGANIZATION_ID, providerOrganizationId);
	}

	public void setProviderServiceId(String providerServiceId) {
		super.put(PROVIDER_SERVICE_ID, providerServiceId);
	}

	public void setProviderSystemId(String providerSystemId) {
		super.put(PROVIDER_SYSTEM_ID, providerSystemId);
	}

	public void setRequestReceiveTime(Calendar requestReceiveTime) {
		super.put(REQUEST_RECEIVE_TIME, requestReceiveTime);
	}

	public void setRequestSendTime(Calendar requestSendTime) {
		super.put(REQUEST_SEND_TIME, requestSendTime);
	}

	public void setResponseReceiveTime(Calendar responseReceiveTime) {
		super.put(RESPONSE_RECEIVE_TIME, responseReceiveTime);
	}

	public void setResponseSendTime(Calendar responseSendTime) {
		super.put(RESPONSE_SEND_TIME, responseSendTime);
	}

	public void setResultCode(ResultCode resultCode) {
		super.put(RESULT_CODE, resultCode.getValue());
	}

	public void setResultMessage(String resultMessage) {
		super.put(RESULT_MESSAGE, resultMessage);
	}
}
