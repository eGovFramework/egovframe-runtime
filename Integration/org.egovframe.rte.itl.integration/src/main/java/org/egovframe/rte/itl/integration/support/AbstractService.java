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
package org.egovframe.rte.itl.integration.support;

import org.egovframe.rte.itl.integration.*;
import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader.ResultCode;
import org.egovframe.rte.itl.integration.EgovIntegrationServiceCallback.CallbackId;
import org.egovframe.rte.itl.integration.message.simple.SimpleMessage;
import org.egovframe.rte.itl.integration.message.simple.SimpleMessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 전자정부 연계 서비스의 Service interface를 구현 추상 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 Service interface를 구현한 abstract Service class이다
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
 * 2017.02.13	장동한				시큐어코딩(ES)-검사시점과 사용시점(TOCTOU)[CWE-367]
 * </pre>
 */
public abstract class AbstractService implements EgovIntegrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);

	/** 연계 ID */
	protected String id;

	/** default timeout */
	protected long defaultTimeout;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            연계 ID
	 * @param defaultTimeout
	 *            default timeout
	 * @throws IllegalArgumentException
	 *             Argument <code>id</code> 값이 <code>null</code>이거나 공백 문자인 경우
	 */
	public AbstractService(String id, long defaultTimeout) {
		super();
		if (StringUtils.hasText(id) == false) {
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.defaultTimeout = defaultTimeout;
	}

	public long getDefaultTimeout() {
		return defaultTimeout;
	}

	public String getId() {
		return id;
	}

	public EgovIntegrationServiceResponse sendAsync(
			final EgovIntegrationMessage requestMessage) {
		LOGGER.debug("sendAsync called without Callback");

		if (requestMessage == null) {
			LOGGER.error("requestMessage is null");
			throw new IllegalArgumentException();
		}

		LOGGER.debug("Create MessageSender without Callback");
		MessageSender sender = new MessageSender(this, requestMessage, null);

		LOGGER.debug("Start MessageSender");
		sender.start();

		return new DefaultResponse(sender, defaultTimeout);
	}

	public void sendAsync(final EgovIntegrationMessage requestMessage,
			EgovIntegrationServiceCallback callback) {
		LOGGER.debug("sendAsync called with Callback");

		if (requestMessage == null) {
			LOGGER.error("requestMessage is null");
			throw new IllegalArgumentException();
		} else if (callback == null) {
			LOGGER.error("callback is null");
			throw new IllegalArgumentException();
		}

		LOGGER.debug("Create MessageSender with Callback");
		MessageSender sender = new MessageSender(this, requestMessage, callback);

		LOGGER.debug("Start MessageSender");
		sender.start();
	}

	public EgovIntegrationMessage sendSync(EgovIntegrationMessage requestMessage) {
		LOGGER.debug("sendSync called without timeout");

		if (requestMessage == null) {
			LOGGER.error("requestMessage is null");
			throw new IllegalArgumentException();
		}

		LOGGER.debug("call sendSync with defauleTimeout");
		return sendSync(requestMessage, defaultTimeout);
	}

	public EgovIntegrationMessage sendSync(
			EgovIntegrationMessage requestMessage, long timeout) {
		LOGGER.debug("sendSync called with timeout");

		if (requestMessage == null) {
			LOGGER.error("requestMessage is null");
			throw new IllegalArgumentException();
		}

		LOGGER.debug("Create MessageSender without Callback");
		MessageSender sender = new MessageSender(this, requestMessage, null);

		LOGGER.debug("Start MessageSender");
		sender.start();

		LOGGER.debug("Wait for the termination of MessageSender");
		try {
			sender.join(timeout);
		} catch (InterruptedException e) {
			LOGGER.debug("MessageSender was interrupted", e);
		}

		if (sender.isAlive()) {
			LOGGER.debug("MessageSender is alive over 'timeout'.");
			sender.interrupt();
			EgovIntegrationMessageHeader responseHeader = new SimpleMessageHeader(
					requestMessage.getHeader());
			responseHeader.setResultCode(ResultCode.TIME_OUT);

			return new SimpleMessage(responseHeader);
		} else {
			LOGGER.debug("MessageSender finished to send and receive messages.");

			return sender.getResponseMessage();
		}
	}

	/**
	 * 실제 메시지를 주고 받는 method이다. timeout 값과 관계 없이 요청 메시지를 보내고 응답 메시지를 받는다. 반드시 모든
	 * Exception을 내부에서 처리해야한다.
	 * 
	 * @param requestMessage
	 *            요청 메시지
	 * @return 응답 메시지
	 */
	protected abstract EgovIntegrationMessage doSend(EgovIntegrationMessage requestMessage);

}

/**
 * <b>Class Name</b> : MessageSender
 * <p>
 * <b>Description</b> : 전자정부 연계 서비스의 AbstractService의 sendAsync를 구현하기 위한
 * MessageSender class이다.
 * </p>
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
 * <p>
 * </p>
 * <b>Copyright (C) 2008 by MOPAS All right reserved.</b>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009. 03. 03
 * @version 1.0
 * @see
 */
class MessageSender extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

	/** Service */
	protected AbstractService service;

	/** 요청 메시지 */
	protected EgovIntegrationMessage requestMessage;

	/** 응답 메시지 */
	protected EgovIntegrationMessage responseMessage = null;

	/** Callback */
	protected EgovIntegrationServiceCallback callback = null;

	/**
	 * MessageSender를 생성한다.
	 * 
	 * @param service
	 *            service
	 * @param requestMessage
	 *            요청 메시지
	 */
	public MessageSender(final AbstractService service,
			final EgovIntegrationMessage requestMessage,
			EgovIntegrationServiceCallback callback) {
		super();
		this.service = service;
		this.requestMessage = requestMessage;
		this.callback = callback;
	}

	/**
	 * 응답메시지
	 * 
	 * @return the responseMessage
	 */
	public EgovIntegrationMessage getResponseMessage() {
		return responseMessage;
	}

	@Override
	public void run() {
		LOGGER.debug("MessageSender just Start");
		//2017.02.13 장동한 시큐어코딩(ES)-검사시점과 사용시점(TOCTOU)[CWE-367]
		synchronized(service){
			CallbackId callbackId = null;
			if (callback != null) {
				callbackId = callback.createId(service, requestMessage);
				LOGGER.debug("Create CallbackId(" + callbackId + ")");
			}
	
			LOGGER.debug("Send and Receive Messages");
			responseMessage = service.doSend(requestMessage);
	
			if (callback != null) {
				LOGGER.debug("Notify to callback");
				callback.onReceive(callbackId, responseMessage);
			}
		}
	}
}

/**
 * <b>Class Name</b> : DefaultResponse
 * <p> </p>
 * <b>Description</b> : 전자정부 연계 서비스의 Response interface를 구현한 class이다.<br>
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
 * <p></p> 
 * </table>
 * <b>Copyright (C) 2008 by MOPAS All right reserved.</b>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009. 03. 03
 * @version 1.0
 * @see
 */
class DefaultResponse implements EgovIntegrationServiceResponse {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResponse.class);

	/** MessageSender */
	protected MessageSender sender;

	/** default timeout(millisecond) */
	protected long defaultTimeout;

	/**
	 * DefaultResponse를 생성한다.
	 * 
	 * @param sender
	 *            MessageSender
	 * @param defaultTimeout
	 *            default timeout(millisecond)
	 */
	public DefaultResponse(final MessageSender sender, final long defaultTimeout) {
		super();
		this.sender = sender;
		this.defaultTimeout = defaultTimeout;
	}

	public EgovIntegrationMessage receive() {
		LOGGER.debug("receive without timeout. Call receive with defaultTimeout");
		return receive(defaultTimeout);
	}

	public EgovIntegrationMessage receive(long timeout) {
		LOGGER.debug("receive with timeout");

		LOGGER.debug("wait for the termination of MessageSender");
		try {
			sender.join(timeout);
		} catch (InterruptedException e) {
			LOGGER.debug("MessageSender was interrupted", e);
		}

		if (sender.isAlive()) {
			LOGGER.debug("MessageSender is alive over 'timeout'");
			sender.interrupt();
			EgovIntegrationMessageHeader responseHeader = new SimpleMessageHeader(
					sender.requestMessage.getHeader());
			responseHeader.setResultCode(ResultCode.TIME_OUT);

			return new SimpleMessage(responseHeader);
		} else {
			LOGGER.debug("MessageSender finished to send and receive messages.");

			return sender.getResponseMessage();
		}
	}
}
