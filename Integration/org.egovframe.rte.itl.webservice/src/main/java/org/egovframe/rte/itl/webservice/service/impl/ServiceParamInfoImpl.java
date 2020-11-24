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

import org.egovframe.rte.itl.integration.type.Type;
import org.egovframe.rte.itl.webservice.service.ServiceParamInfo;

import javax.jws.WebParam.Mode;

/**
 * 웹 서비스 ServiceEndpointInterface의 각 parameter와 EgovIntegrationMessage의 인터페이스 구현
 * 클래스.
 * <p>
 * <b>NOTE:</b> 웹 서비스 ServiceEndpointInterface의 각 parameter와
 * EgovIntegrationMessage의 각 field 간의 mapping 정보를 나타내는 class이다.
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
public class ServiceParamInfoImpl implements ServiceParamInfo {

	/** name */
	private String name;

	/** type */
	private Type type;

	/** mode */
	private Mode mode;

	/** header flag */
	private boolean header;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name
	 * @param type
	 *            type
	 * @param mode
	 *            mode
	 * @param header
	 *            header flag
	 */
	public ServiceParamInfoImpl(String name, Type type, Mode mode, boolean header) {
		super();
		this.name = name;
		this.type = type;
		this.mode = mode;
		this.header = header;
	}

	/**
	 * name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * type
	 * 
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * mode
	 * 
	 * @return the mode
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * mode
	 * 
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	/**
	 * header
	 * 
	 * @return the header
	 */
	public boolean isHeader() {
		return header;
	}

}
