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

import java.util.HashMap;
import java.util.Map;

import egovframework.rte.itl.integration.EgovIntegrationMessage;
import egovframework.rte.itl.integration.EgovIntegrationMessageHeader;
import egovframework.rte.itl.integration.type.RecordType;
import egovframework.rte.itl.integration.type.Type;

import org.springframework.util.StringUtils;

/**
 * 전자정부 연계 서비스의 표준 Message 구현 클래스.
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 Message interface를 구현한 Message class이다.
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
public final class TypedMessage extends TypedMap implements EgovIntegrationMessage {

	/** TYPE_ID */
	public static final String TYPE_ID = "__message_type__";

	/** TYPE_NAME */
	public static final String TYPE_NAME = "$$Egov$$TypedMessageType";

	/** HEADER_FIELD_NAME */
	public static final String HEADER_FIELD_NAME = "header";

	/** BODY_FIELD_NAME */
	public static final String BODY_FIELD_NAME = "body";

	/** Attachments */
	protected Map<String, Object> attachments = new HashMap<String, Object>();

	/**
	 * <code>bodyType</code>을 바디부의 Type으로 갖는 TypedMessage를 생성한다.
	 * 
	 * @param bodyType
	 *            바디부의 Type
	 * @throws IllegalArgumentException
	 *             Argument <code>bodyType</code> 값이 <code>null</code>인 경우
	 */
	public TypedMessage(final RecordType bodyType) {
		super(new RecordType(TYPE_ID, TYPE_NAME, new HashMap<String, Type>() {
			/**
			 * serialVersion UID
			 */
			private static final long serialVersionUID = -736199397270114147L;

			{
				put(HEADER_FIELD_NAME, TypedMessageHeader.TYPE);
				put(BODY_FIELD_NAME, bodyType);
			}
		}));

		put(HEADER_FIELD_NAME, new TypedMessageHeader());
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getBody() {
		return (Map<String, Object>) get(BODY_FIELD_NAME);
	}

	public EgovIntegrationMessageHeader getHeader() {
		return (EgovIntegrationMessageHeader) get(HEADER_FIELD_NAME);
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
		put(BODY_FIELD_NAME, body);
	}

	public void setHeader(EgovIntegrationMessageHeader header) {
		if (header == null) {
			throw new IllegalArgumentException();
		}
		put(HEADER_FIELD_NAME, header);
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
