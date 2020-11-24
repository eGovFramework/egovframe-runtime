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
package org.egovframe.rte.itl.integration.metadata;

import java.util.concurrent.atomic.AtomicBoolean;

import org.egovframe.rte.itl.integration.util.Validatable;

import org.springframework.util.StringUtils;

/**
 * 전자정부 연계 서비스 메타 데이터 중 '레코트 타입 필드' 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 메타 데이터 중 '레코트 타입 필드'를 나타내는 class이다.
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
public class RecordTypeFieldDefinition implements Validatable {

	/** 필드 타입 ID */
	private String typeId;

	/** valid */
	private boolean valid = false;

	/** statucChanged flag */
	private AtomicBoolean statusChanged = new AtomicBoolean(false);

	/**
	 * Default Constructor
	 */
	public RecordTypeFieldDefinition() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param typeId
	 *            필드 타입 ID
	 */
	public RecordTypeFieldDefinition(String typeId) {
		super();
		this.typeId = typeId;
		this.statusChanged.set(true);
	}

	/**
	 * typeId
	 * 
	 * @return the typeId
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * typeId
	 * 
	 * @param typeId
	 *            the typeId to set
	 */
	public void setTypeId(String typeId) {
		this.typeId = typeId;
		this.statusChanged.set(true);
	}

	public boolean isValid() {
		if (statusChanged.getAndSet(false)) {
			// valid = (StringUtils.hasText(name) &&
			// StringUtils.hasText(typeId));
			valid = StringUtils.hasText(typeId);
		}
		return valid;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName()).append(" {typeId = ").append(StringUtils.quote(typeId)).append("}");
		return sb.toString();
	}

}
