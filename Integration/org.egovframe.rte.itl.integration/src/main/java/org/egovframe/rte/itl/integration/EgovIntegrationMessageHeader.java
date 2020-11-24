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
package org.egovframe.rte.itl.integration;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 전자정부 연계 서비스의 표준 메시지 헤더 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지 헤더 interface이다.
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
public interface EgovIntegrationMessageHeader {

	/**
	 * <b>Class Name</b> : ResultCode
	 * <p>
	 * <b>Description</b> : 전자정부 연계 서비스 결과 코드를 나타내는 enum이다. </p>
	 * <p>
	 * <table border="1">
	 * <caption><b>Modification Information</b></caption>
	 * <tr bgcolor="bbbbbb">
	 * <th>수정일</th>
	 * <th>수정자</th>
	 * <th>수정내용</th>
	 * </tr>
	 * <tr>
	 * <td>2009.03.03</td>
	 * <td>심상호</td>
	 * <td>최초 생성</td>
	 * </tr>
	 * </table>
	 * </p>
	 */
	public enum ResultCode {
		/**
		 * 정상 종료 : <br>
		 * 연계가 정상적으로 종료된 경우
		 */
		OK("0000"),

		/**
		 * Timeout 발생 : <br>
		 * 연계 수행 중 Client 단에서 Timeout이 발생한 경우
		 */
		TIME_OUT("0001"),

		/**
		 * 업무 오류 발생 : <br>
		 * 연계 수행 중 Server 단에서 업무적인 오류가 발생한 경우
		 */
		BUSINESS_ERROR("0002"),

		// Configuration
		/**
		 * 사용하지 않는 연계 : <br>
		 * 연계 정의(IntegrationDefinition)의 <code>using</code> flag가
		 * <code>false</code>인 경우
		 */
		NOT_USABLE_INTEGRATION("1000"),

		/**
		 * 연계 가용시간이 아님 : <br>
		 * 연계를 요청한 시각이 연계 정의(IntegrationDefinition)의 <code>validateFrom</code>와
		 * <code>validateTo</code> 사이가 아닌 경우
		 * <p>
		 * <table border="1">
		 * <caption> <b> 연계 가용시각 조건 ( <code>now : Calendar</code> = 현재 시각) </b>
		 * </caption>
		 * <tr bgcolor="bbbbbb">
		 * <th><code>validateFrom</code></th>
		 * <th><code>validateTo</code></th>
		 * <th>valid condition</th>
		 * </tr>
		 * <tr>
		 * <td><code>null</code></td>
		 * <td><code>null</code></td>
		 * <td><code>= true</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>from : Calendar</code></td>
		 * <td><code>null</code></td>
		 * <td><code>= (from.compareTo(now) <=
		 * 0)</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>null</code></td>
		 * <td><code>to : Calendar</code></td>
		 * <td><code>= (now.compareTo(to) <= 0)</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>from : Calendar</code></td>
		 * <td><code>to : Calendar</code></td>
		 * <td><code>= (from.compareTo(now> <= 0 &&
		 * now.compareTo(to) <= 0) </code></td>
		 * </tr>
		 * </table>
		 * </p>
		 */
		INVALID_TIME("1001"),

		/**
		 * 사용하지 않는 서비스 : <br>
		 * 연계 정의(IntegrationDefinition)에 등록된 제공 서비스(ServiceDefinition)의
		 * <code>using</code> flag 값이 <code>false</code>인 경우
		 */
		NOT_USABLE_SERVICE("1002"),

		/**
		 * 기대하지 않은 연계 요청자 : <br>
		 * 연계 제공 시스템(Server)에 등록된 연계 정의(IntegrationDefinition)의 요청 시스템 코드값과 요청
		 * 메시지 헤더의 요청 시스템 코드값이 일치하지 않는 경우
		 */
		UNEXPECTED_CONSUMER("1003"),

		/**
		 * 기대하지 않은 연계 제공자 : <br>
		 * 연계 제공 시스템(Server)에 등록된 시스템 코드와 요청 메시지 헤더의 제공 시스템 코드값이 일치하지 않는 경우
		 */
		UNEXPECTED_PROVIDER("1004"),

		/**
		 * 제공하지 않는 서비스 : <br>
		 * 연계 제공 시스템(Server)에 등록되어 있지 않은 서비스를 요청한 경우
		 */
		NO_SUCH_SERVICE("1005"),

		/**
		 * 연계 서비스 초기화 실패 : <br>
		 * 연계 제공 서비스를 초기화하는데 실패한 경우
		 */
		FAIL_IN_INITIALIZING("1006"),

		// send request
		/**
		 * 요청 메시지 생성 실패 : <br>
		 * Client에서 요청 메시지를 생성할 때 오류가 발생한 경우
		 */
		FAIL_IN_CREATING_REQUEST_MESSAGE("2000"),

		/**
		 * 요청 메시지 송신 실패 : <br>
		 * Client에서 요청 메시지를 송신할 때 오류가 발생한 경우
		 */
		FAIL_IN_SENDING_REQUEST("2001"),

		// receive request
		/**
		 * 요청 메시지 수신 실패 : <br>
		 * Server에서 요청 메시지를 수신할 때 오류가 발생한 경우
		 */
		FAIL_IN_RECEIVING_REQUEST("3000"),

		/**
		 * 요청 메시지 분석 실패 : <br>
		 * Server에서 요청 메시지를 분석할 때 오류가 발생한 경우
		 */
		FAIL_IN_PARSING_REQUEST_MESSAGE("3001"),

		/**
		 * 요청 메시지 헤더 부재 : <br>
		 * Server에서 받은 요청 메시지에 표준 메시지 헤더가 존재하지 않는 경우
		 */
		NO_MESSAGE_HEADER_IN_REQUEST("3002"),

		/**
		 * 서비스 제공 모듈 호출 실패 : <br>
		 * Server에서 서비스 제공 모듈을 호출할 때 오류가 발생한 경우
		 */
		FAIL_TO_CALL_SERVICE_PROVIDER("3003"),

		// send response
		/**
		 * 응답 메시지 생성 실패 : <br>
		 * Server에서 응답 메시지를 생성할 때 오류가 발생한 경우
		 */
		FAIL_IN_CREATING_RESPONSE_MESSAGE("4000"),

		/**
		 * 응답 메시지 송신 실패 : <br>
		 * Server에서 응답 메시지를 송신할 때 오류가 발생한 경우
		 */
		FAIL_IN_SENDING_RESPONSE("4001"),

		// receive response
		/**
		 * 응답 메시지 수신 실패 : <br>
		 * Client에서 응답 메시지를 수신할 때 오류가 발생한 경우
		 */
		FAIL_IN_RECEIVING_RESPONSE("5000"),

		/**
		 * 응답 메시지 분석 실패 : <br>
		 * Client에서 응답 메시지를 분석할 때 오류가 발생한 경우
		 */
		FAIL_IN_PARSING_RESPONSE_MESSAGE("5001");

		/** value */
		private String value;

		/**
		 * Constructor
		 * 
		 * @param value
		 *            value
		 */
		private ResultCode(String value) {
			this.value = value;
		}

		/**
		 * value
		 * 
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

		/** codes */
		private static Map<String, ResultCode> codes = new HashMap<String, ResultCode>();

		static {
			for (ResultCode resultCode : ResultCode.values()) {
				codes.put(resultCode.value, resultCode);
			}
		}

		/**
		 * <code>value</code> 값에 해당하는 ResultCode를 return한다.
		 * 
		 * @param value
		 *            value
		 * @return ResultCode
		 */
		public static ResultCode getCode(final String value) {
			return codes.get(value);
		}
	}

	/**
	 * 연계 Id를 읽어온다.
	 * 
	 * @return 연계 Id
	 */
	public String getIntegrationId();

	/**
	 * 연계 Id를 setting한다.
	 * 
	 * @param integrationId
	 *            연계 Id
	 */
	public void setIntegrationId(final String integrationId);

	/**
	 * 제공 기관 Id를 읽어온다.
	 * 
	 * @return 제공 기관 Id
	 */
	public String getProviderOrganizationId();

	/**
	 * 제공 기관 Id를 setting한다.
	 * 
	 * @param providerOrganizationId
	 *            제공 기관 Id
	 */
	public void setProviderOrganizationId(final String providerOrganizationId);

	/**
	 * 제공 시스템 Id를 읽어온다.
	 * 
	 * @return 제공 시스템 Id
	 */
	public String getProviderSystemId();

	/**
	 * 제공 시스템 Id를 setting한다.
	 * 
	 * @param providerSystemId
	 *            제공 시스템 Id
	 */
	public void setProviderSystemId(final String providerSystemId);

	/**
	 * 제공 서비스 Id를 읽어온다.
	 * 
	 * @return 제공 서비스 Id
	 */
	public String getProviderServiceId();

	/**
	 * 제공 서비스 Id를 setting한다.
	 * 
	 * @param providerServiceId
	 *            제공 서비스 Id
	 */
	public void setProviderServiceId(final String providerServiceId);

	/**
	 * 요청 기관 Id를 읽어온다.
	 * 
	 * @return 요청 기관 Id
	 */
	public String getConsumerOrganizationId();

	/**
	 * 요청 기관 Id를 setting한다.
	 * 
	 * @param consumerOrganizationId
	 *            요청 기관 Id
	 */
	public void setConsumerOrganizationId(final String consumerOrganizationId);

	/**
	 * 요청 시스템 Id를 읽어온다.
	 * 
	 * @return 요청 시스템 Id
	 */
	public String getConsumerSystemId();

	/**
	 * 요청 시스템 Id를 setting한다.
	 * 
	 * @param consumerSystemId
	 *            요청 시스템 Id
	 */
	public void setConsumerSystemId(final String consumerSystemId);

	/**
	 * 요청 송신 시각을 읽어온다.
	 * 
	 * @return 요청 송신 시각
	 */
	public Calendar getRequestSendTime();

	/**
	 * 요청 송신 시각을 setting한다.
	 * 
	 * @param requestSendTime
	 *            요청 송신 시각
	 */
	public void setRequestSendTime(final Calendar requestSendTime);

	/**
	 * 요청 수신 시각을 읽어온다.
	 * 
	 * @return 요청 수신 시각
	 */
	public Calendar getRequestReceiveTime();

	/**
	 * 요청 수신 시각을 setting한다.
	 * 
	 * @param requestReceiveTime
	 *            요청 수신 시각
	 */
	public void setRequestReceiveTime(final Calendar requestReceiveTime);

	/**
	 * 응답 송신 시각을 읽어온다.
	 * 
	 * @return 응답 송신 시각
	 */
	public Calendar getResponseSendTime();

	/**
	 * 응답 송신 시각을 setting한다.
	 * 
	 * @param responseSendTime
	 *            응답 송신 시각
	 */
	public void setResponseSendTime(final Calendar responseSendTime);

	/**
	 * 응답 수신 시각을 읽어온다.
	 * 
	 * @return 응답 수신 시각
	 */
	public Calendar getResponseReceiveTime();

	/**
	 * 응답 수신 시각을 setting한다.
	 * 
	 * @param responseReceiveTime
	 *            응답 수신 시각
	 */
	public void setResponseReceiveTime(final Calendar responseReceiveTime);

	/**
	 * 결과 코드를 읽어온다.
	 * 
	 * @return 결과 코드
	 */
	public ResultCode getResultCode();

	/**
	 * 결과 코드를 setting한다.
	 * 
	 * @param resultCode
	 *            결과 코드
	 */
	public void setResultCode(final ResultCode resultCode);

	/**
	 * 결과 메시지를 읽어온다.
	 * 
	 * @return 결과 메시지
	 */
	public String getResultMessage();

	/**
	 * 결과 메시지를 setting한다.
	 * 
	 * @param resultMessage
	 *            결과 메시지
	 */
	public void setResultMessage(final String resultMessage);

}
