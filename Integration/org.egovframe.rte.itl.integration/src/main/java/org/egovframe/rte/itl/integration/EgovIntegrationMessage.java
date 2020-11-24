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

import java.util.Map;

/**
 * 전자정부 연계 서비스의 표준 메시지 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지 interface이다. </p>
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
public interface EgovIntegrationMessage {

	/**
	 * 메시지 헤더를 읽어온다.
	 * 
	 * @return 메시지 헤더
	 */
	public EgovIntegrationMessageHeader getHeader();

	/**
	 * 메시지 헤더를 setting한다.
	 * 
	 * @param header
	 *            메시지 헤더 (not null)
	 * @throws IllegalArgumentException
	 *             Argument <code>header</code> 값이 <code>null</code>인 경우
	 */
	public void setHeader(final EgovIntegrationMessageHeader header);

	/**
	 * 메시지 바디를 읽어온다.
	 * 
	 * @return 메시지 바디
	 */
	public Map<String, Object> getBody();

	/**
	 * 메시지 바디를 setting한다.
	 * 
	 * @param body
	 *            메시지 바디 (not null)
	 * @throws IllegalArgumentException
	 *             Argument <code>body</code> 값이 <code>null</code>인 경우
	 */
	public void setBody(final Map<String, Object> body);

	/**
	 * 메시지에 첨부된 내용을 읽어온다.
	 * 
	 * @return attachments
	 */
	public Map<String, Object> getAttachments();

	/**
	 * 메시지에 첨부할 내용을 setting한다.
	 * 
	 * @param attachments
	 *            attachments (not null)
	 * @throws IllegalArgumentException
	 *             1. Argument <code>attachments</code> 값이 <code>null</code>인 경우 <br>
	 *             2. Argument <code>attachments</code>의 <code>key</code> 값들 중
	 *             <code>null</code> 값 또는 공백 문자가 있는 경우 <br>
	 *             3. Argument <code>attachments</code>의 <code>value</code> 값들 중
	 *             <code>null</code> 값이 있는 경우
	 */
	public void setAttachments(final Map<String, Object> attachments);

	/**
	 * 메시지에 첨부된 내용을 읽어온다.
	 * 
	 * @param name
	 *            name of attachment
	 * @return attachment
	 */
	public Object getAttachment(final String name);

	/**
	 * 메시지에 첨부를 추가한다.
	 * 
	 * @param name
	 *            name of attachment
	 * @param attachment
	 *            attachment
	 * @return 기존에 같은 이름으로 추가되어 있던 attachment
	 * @throws IllegalArgumentException
	 *             1. Argument <code>name</code> 값이 <code>null</code>이거나 공백 문자로
	 *             이루어져 있는 경우 <br>
	 *             2. Argument <code>attachment</code> 값이 <code>null</code>인 경우
	 */
	public Object putAttachment(final String name, final Object attachment);

	/**
	 * 메시지에서 첨부를 삭제한다.
	 * 
	 * @param name
	 *            name of attachment
	 * @return 삭제된 attachment
	 */
	public Object removeAttachment(final String name);

}
