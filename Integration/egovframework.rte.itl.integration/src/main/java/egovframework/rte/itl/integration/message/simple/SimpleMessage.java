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
package egovframework.rte.itl.integration.message.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import egovframework.rte.itl.integration.EgovIntegrationMessage;
import egovframework.rte.itl.integration.EgovIntegrationMessageHeader;

import org.springframework.util.StringUtils;

/**
 * 전자정부 연계 서비스의 표준 메시지 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지 Class이다.
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
public class SimpleMessage implements EgovIntegrationMessage {

	/** 메시지 헤더 */

	protected EgovIntegrationMessageHeader header = null;

	/** 메시지 바디 */
	protected Map<String, Object> body = new HashMap<String, Object>();

	/** Attachment */
	protected Map<String, Object> attachments = new HashMap<String, Object>();

	/**
	 * . Default Constructor
	 */
	public SimpleMessage() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param header
	 *            메시지 헤더
	 * @throws IllegalArgumentException
	 *             Argument <code>header</code> 값이 <code>null</code>인 경우
	 */
	public SimpleMessage(final EgovIntegrationMessageHeader header) {
		super();
		setHeader(header);
	}

	/**
	 * Constructor.
	 * 
	 * @param header
	 *            메시지 헤더
	 * @param body
	 *            메시지 바디
	 * @throws IllegalArgumentException
	 *             1. Argument <code>header</code>, <code>body</code> 값이
	 *             <code>null</code>인 경우 <br>
	 *             2. Argument <code>body</code>의 <code>key</code> 값 중
	 *             <code>null</code> 또는 공백 문자가 있는 경우 <br>
	 *             3. Argument <code>body</code>의 <code>value</code> 값 중
	 *             <code>null</code> 값이 있는 경우
	 */
	public SimpleMessage(final EgovIntegrationMessageHeader header, final Map<String, Object> body) {
		this(header);
		setBody(body);
	}

	/**
	 * Constructor.
	 * 
	 * @param header
	 *            메시지 헤더
	 * @param body
	 *            메시지 바디
	 * @param attachments
	 *            첨부
	 * @throws IllegalArgumentException
	 *             1. Argument <code>header</code>, <code>body</code>,
	 *             <code>attachments</code> 값이 <code>null</code>인 경우 <br>
	 *             2. Argument <code>body</code>, <code>attachments</code>의
	 *             <code>key</code> 값 중 <code>null</code> 또는 공백 문자가 있는 경우 <br>
	 *             3. Argument <code>body</code>, <code>attachments</code>의
	 *             <code>value</code> 값 중 <code>null</code> 값이 있는 경우
	 */
	public SimpleMessage(final EgovIntegrationMessageHeader header, final Map<String, Object> body, final Map<String, Object> attachments) {
		this(header, body);
		setAttachments(attachments);
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		if (body == null) {
			throw new IllegalArgumentException();
		}
		for (Entry<String, Object> entry : body.entrySet()) {
			if (StringUtils.hasText(entry.getKey()) == false) {
				throw new IllegalArgumentException();
			}
		}
		this.body = body;
	}

	public EgovIntegrationMessageHeader getHeader() {
		return header;
	}

	public void setHeader(EgovIntegrationMessageHeader header) {
		if (header == null) {
			throw new IllegalArgumentException();
		}
		this.header = header;
	}

	public Object getAttachment(String name) {
		return attachments.get(name);
	}

	public Map<String, Object> getAttachments() {
		return attachments;
	}

	public Object putAttachment(String name, Object attachment) {
		if (StringUtils.hasText(name) == false) {
			throw new IllegalArgumentException();
		}
		// else if (attachment == null)
		// {
		// throw new IllegalArgumentException();
		// }
		return attachments.put(name, attachment);
	}

	public Object removeAttachment(String name) {
		return attachments.remove(name);
	}

	public void setAttachments(Map<String, Object> attachments) {
		if (attachments == null) {
			throw new IllegalArgumentException();
		}
		for (Entry<String, Object> entry : attachments.entrySet()) {
			if (StringUtils.hasText(entry.getKey()) == false) {
				throw new IllegalArgumentException();
			}
			// else if (entry.getValue() == null)
			// {
			// throw new IllegalArgumentException();
			// }
		}
		this.attachments = attachments;
	}
}
