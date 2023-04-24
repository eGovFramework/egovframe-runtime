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

import org.egovframe.rte.itl.integration.util.Validatable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 연계 서비스 메타 데이터 중 '레코트 타입' 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 메타 데이터 중 '레코트 타입'을 나타내는 class이다. RecordType를 구성하기 위한
 * 정의 정보를 담고 있다. RecordTypeDefinition은 다른 RecordTypeDefinition의 형식을 상속받을 수 있다.
 * 만약 같은 이름의 field가 정의되어 있을 경우 자식의 field 정의를 따른다.
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
public class RecordTypeDefinition implements Validatable {

	/** id */
	private String id;

	/** 부모 RecordTypeDefinition */
	private RecordTypeDefinition parent;

	/** Record Type 명 */
	private String name;

	/** field 정의 map */
	private Map<String, RecordTypeFieldDefinition> fields = new HashMap<String, RecordTypeFieldDefinition>();

	/** valid */
	private boolean valid = false;

	/** statucChanged flag */
	private AtomicBoolean statusChanged = new AtomicBoolean(false);

	/**
	 * Default Constructor
	 */
	public RecordTypeDefinition() {
		super();
	}

	/**
	 * Argument <code>id</code>를 id로 갖는 <code>RecordTypeDefinition</code> 객체를
	 * 생성한다.
	 * 
	 * @param id
	 *            id
	 * @param name
	 *            name
	 */
	public RecordTypeDefinition(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.statusChanged.set(true);
	}

	/**
	 * Argument <code>id</code>를 id로 갖고, Argument <code>parent</code>의 형식을 상속받는
	 * <code>RecordTypeDefinition</code> 객체를 생성한다.
	 * 
	 * @param id
	 *            id
	 * @param name
	 *            name
	 * @param parent
	 *            부모 <code>RecordTypeDefinition</code> 객체
	 */
	public RecordTypeDefinition(String id, String name,
			RecordTypeDefinition parent) {
		super();
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.statusChanged.set(true);
	}

	/**
	 * Argument <code>id</code>를 id로 갖고, Argument <code>parent</code>의 형식을
	 * 상속받으며, Argument <code>fieldTypeIds</code>의 field 정의 map 을 갖는
	 * <code>RecordTypeDefinition</code> 객체를 생성한다.
	 * 
	 * @param id
	 *            id
	 * @param name
	 *            name
	 * @param parent
	 *            부모 <code>RecordTypeDefinition</code> 객체
	 * @param fields
	 *            field 정의 map
	 */
	public RecordTypeDefinition(String id, String name,
			RecordTypeDefinition parent,
			Map<String, RecordTypeFieldDefinition> fields) {
		super();
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.fields = fields;
		this.statusChanged.set(true);
	}

	/**
	 * id
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * id
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
		this.statusChanged.set(true);
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
	 * name
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
		this.statusChanged.set(true);
	}

	/**
	 * parent
	 * 
	 * @return the parent
	 */
	public RecordTypeDefinition getParent() {
		return parent;
	}

	/**
	 * parent
	 * 
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(RecordTypeDefinition parent) {
		this.parent = parent;
		this.statusChanged.set(true);
	}

	/**
	 * fields
	 * 
	 * @return the fields
	 */
	public Map<String, RecordTypeFieldDefinition> getFields() {
		return fields;
	}

	/**
	 * fields
	 * 
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(Map<String, RecordTypeFieldDefinition> fields) {
		this.fields = fields;
		this.statusChanged.set(true);
	}

	/**
	 * Field type을 읽어온다.
	 * 
	 * @param fieldName
	 *            field 명
	 * @return <code>fieldName</code>의 type, 존재하지 않을 경우 <code>null</code>
	 */
	public RecordTypeFieldDefinition getField(String fieldName) {
		return fields.get(fieldName);
	}

	/**
	 * Field 정의를 추가한다.
	 * 
	 * @param fieldName
	 *            field 명
	 * @param field
	 *            field definition
	 * @return 기존에 <code>fieldName</code>으로 정의된 field가 있을 경우 기존 field
	 *         definition, 없을 경우 <code>null</code>
	 */
	public RecordTypeFieldDefinition putField(String fieldName,
			RecordTypeFieldDefinition field) {
		this.statusChanged.set(true);
		return fields.put(fieldName, field);
	}

	/**
	 * Field 정의를 삭제한다.
	 * 
	 * @param fieldName
	 *            field 명
	 * @return <code>fieldName</code>으로 정의된 field definition, 없을 경우
	 *         <code>null</code>
	 */
	public RecordTypeFieldDefinition removeField(String fieldName) {
		this.statusChanged.set(true);
		return fields.remove(fieldName);
	}

	public boolean isValid() {
		if (statusChanged.getAndSet(false)) {
			valid = (StringUtils.hasText(id) && StringUtils.hasText(name) && fields != null);
			if (fields != null) {
				for (RecordTypeFieldDefinition field : fields.values()) {
					valid = valid && field.isValid();
				}
			}
			if (parent != null) {
				valid = valid && parent.isValid();
			}
		}
		return valid;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName()).append(" {").append("\n\tid = ")
				.append(StringUtils.quote(id)).append("\n\tname = ")
				.append(StringUtils.quote(name)).append("\n\tparent = ")
				.append(parent == null ? "" : "\n").append(parent);
		if (fields == null) {
			sb.append("\n\tfields = null");
		} else {
			sb.append("\n\tfields = {");
			for (Entry<String, RecordTypeFieldDefinition> entry : fields
					.entrySet()) {
				sb.append("\n\t\t<key = ")
						.append(StringUtils.quote(entry.getKey()))
						.append(", value = ")
						.append(entry.getValue() == null ? "" : "\n")
						.append(entry.getValue()).append(">");
			}
			sb.append("\n\t}");
		}
		sb.append("\n}");
		return sb.toString();
	}

}
