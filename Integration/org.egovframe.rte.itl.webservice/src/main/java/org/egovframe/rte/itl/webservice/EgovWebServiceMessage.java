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

import java.util.Map;

import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader;
import org.egovframe.rte.itl.integration.message.simple.SimpleMessage;

/**
 * 전자정부 웹 서비스 메시지 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 웹 서비스 메시지 class이다.
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
public class EgovWebServiceMessage extends SimpleMessage {

	/**
	 * Constructor
	 * 
	 * @param header
	 *            메시지 헤더
	 */
	public EgovWebServiceMessage(EgovIntegrationMessageHeader header) {
		super(header);
	}

	/**
	 * Constructor
	 * 
	 * @param header
	 *            메시지 헤더
	 * @param body
	 *            메시지 바디
	 * @param attachments
	 *            첨부
	 */
	public EgovWebServiceMessage(EgovIntegrationMessageHeader header, Map<String, Object> body, Map<String, Object> attachments) {
		super(header, body, attachments);
	}

	/**
	 * Constructor
	 * 
	 * @param header
	 *            메시지 헤더
	 * @param body
	 *            메시지 바디
	 */
	public EgovWebServiceMessage(EgovIntegrationMessageHeader header, Map<String, Object> body) {
		super(header, body);
	}

}
