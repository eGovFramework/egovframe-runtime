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
package org.egovframe.rte.itl.webservice.data;

import java.util.concurrent.atomic.AtomicBoolean;

import org.egovframe.rte.itl.integration.util.Validatable;

import org.springframework.util.StringUtils;

/**
 * 전자정부 웹 서비스 설정 정보 중 Param 매핑 정보 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 웹 서비스 설정 정보 중 Param 매핑 정보를 나타내는 class이다. </p>
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
public class MappingInfo implements Validatable {

	/** type id */
	private String type;

	/** index */
	private int index;

	/** argument name */
	private String argumentName;

	/** header flag */
	private boolean header;

	/** valid */
	private boolean valid = false;

	/** statucChanged flag */
	private AtomicBoolean statusChanged = new AtomicBoolean(false);

	/**
	 * Default Constructor
	 */
	public MappingInfo() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type
	 * @param index
	 *            index
	 * @param argumentName
	 *            argument name
	 * @param header
	 *            header flag
	 */
	public MappingInfo(String type, int index, String argumentName, boolean header) {
		super();
		this.type = type;
		this.index = index;
		this.argumentName = argumentName;
		this.header = header;
		this.statusChanged.set(true);
	}

	/**
	 * type
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * type
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
		this.statusChanged.set(true);
	}

	/**
	 * index
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * index
	 * 
	 * @param index
	 *            the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * argumentName
	 * 
	 * @return the argumentName
	 */
	public String getArgumentName() {
		return argumentName;
	}

	/**
	 * argumentName
	 * 
	 * @param argumentName
	 *            the argumentName to set
	 */
	public void setArgumentName(String argumentName) {
		this.argumentName = argumentName;
		this.statusChanged.set(true);
	}

	/**
	 * header
	 * 
	 * @return the header
	 */
	public boolean isHeader() {
		return header;
	}

	/**
	 * header
	 * 
	 * @param header
	 *            the header to set
	 */
	public void setHeader(boolean header) {
		this.header = header;
	}

	public boolean isValid() {
		if (statusChanged.getAndSet(false)) {
			valid = (StringUtils.hasText(type) && StringUtils.hasText(argumentName));
		}
		return valid;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName()).append(" {").append("\n\ttype = ")
				.append(StringUtils.quote(type)).append("\n\tindex = ")
				.append(index).append("\n\targumentNAme = ")
				.append(StringUtils.quote(argumentName))
				.append("\n\theader = ").append(header).append("\n}");
		return sb.toString();
	}

}
