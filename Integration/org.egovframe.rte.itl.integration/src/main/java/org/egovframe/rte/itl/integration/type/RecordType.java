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
package org.egovframe.rte.itl.integration.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.egovframe.rte.itl.integration.message.typed.TypedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 연계 서비스의 표준 메시지를 정의 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지를 정의하기 위한 Class 이다. Colleciton Type 중 Record
 * Type을 정의하기 위한 Class이다. Record Type은 {@literal <key, value>}쌍의 값을 가질 수 있다.
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
public class RecordType extends AbstractType {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordType.class);

	/** RecordType에 Assign할 수 있는 Java 객체 Classes */
	protected static final Class<?>[] recordTypeAssignableClasses = new Class<?>[] {TypedMap.class, Map.class };

	/** field type map */
	protected Map<String, Type> fieldTypes = new HashMap<String, Type>();

	/**
	 * Default Constructor
	 */
	public RecordType() {
		super();
	}

	/**
	 * Argument <code>id</code>를 id로 갖는 RecordType을 생성한다.<br>
	 * TypeLoader에서만 사용된다.
	 * 
	 * @param id
	 *            id
	 * @param name
	 *            name
	 * @throws IllegalArgumentException
	 *             1. Argument <code>id</code> 값이 <code>null</code>이거나, 공백 문자로만
	 *             이루어진 경우 2. Argument <code>name</code> 값이 <code>null</code>
	 *             이거나, 공백 문자로만 이루어진 경우
	 */
	public RecordType(final String id, final String name) {
		super(id, name, recordTypeAssignableClasses);
	}

	/**
	 * <code>id</code>와 <code>fieldTypes</code>를 갖는 RecordType을 생성한다.
	 * 
	 * @param id
	 *            id
	 * @param fieldTypes
	 *            fieldTypes
	 * @throws IllegalArgumentException
	 *             1. Argument <code>id</code> 값이 <code>null</code>이거나, 공백 문자로만
	 *             이루어진 경우 2. Argument <code>name</code> 값이 <code>null</code>
	 *             이거나, 공백 문자로만 이루어진 경우 3. <code>fieldTypes</code> 값이
	 *             <code>null</code>인 경우
	 */
	public RecordType(final String id, final String name,
			final Map<String, Type> fieldTypes) {
		super(id, name, recordTypeAssignableClasses);
		if (fieldTypes == null) {
			LOGGER.error("Argument 'fieldTypes' is null");
			throw new IllegalArgumentException();
		}
		this.fieldTypes = fieldTypes;
	}

	/**
	 * fieldTypes
	 * 
	 * @return the fieldTypes
	 */
	public Map<String, Type> getFieldTypes() {
		return fieldTypes;
	}

	/**
	 * fieldTypes
	 * 
	 * @param fieldTypes
	 *            the fieldTypes to set
	 * @throws <code>fieldTypes</code> 값이 <code>null</code>인 경우
	 */
	public void setFieldTypes(Map<String, Type> fieldTypes) {
		if (fieldTypes == null) {
			LOGGER.error("Argument 'fieldTypes' is null");
			throw new IllegalArgumentException();
		}
		this.fieldTypes = fieldTypes;
	}

	/**
	 * Field의 Type을 읽어온다.
	 * 
	 * @param fieldName
	 *            Field 명
	 * @return <code>fieldName</code>로 정의되어 있는 Field의 Type
	 * @trhows NoSuchFieldException <code>fieldName</code>으로 정의된 Field가 없을 경우
	 */
	public Type getFieldType(final String fieldName) {
		Type fieldType = fieldTypes.get(fieldName);
		if (fieldType == null) {
			throw new NoSuchRecordFieldException();
		}
		return fieldType;
	}

	@SuppressWarnings("unchecked")
	public Object convertToTypedObject(final Object source) {
		if (source == null) {
			return null;
		}
		if (source instanceof TypedMap) {
			if (this.equals(((TypedMap) source).getType()) == false) {
				throw new UnassignableValueException();
			}
			return source;
		}
		if (source instanceof Map) {
			for (Entry<?, ?> entry : ((Map<?, ?>) source).entrySet()) {
				if ((entry.getKey() instanceof String) == false) {
					throw new UnassignableValueException();
				}
			}
			return new TypedMap(this,
					(Map<? extends String, ? extends Object>) source);
		}
		throw new UnassignableValueException();
	}

	public boolean isAssignableValue(Object source) {
		if (source == null) {
			return true;
		}
		if (source instanceof TypedMap) {
			return this.equals(((TypedMap) source).getType());
		}
		if (source instanceof Map) {
			for (Entry<?, ?> entry : ((Map<?, ?>) source).entrySet()) {
				if ((entry.getKey() instanceof String) == false) {
					return false;
				}
				String fieldName = (String) entry.getKey();
				if (getFieldType(fieldName).isAssignableValue(entry.getValue()) == false) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isValid() {
		if (super.isValid() == false || fieldTypes == null) {
			return false;
		}
		for (Type fieldType : fieldTypes.values()) {
			if (fieldType.isValid() == false) {
				return false;
			}
		}
		return true;
	}

}
