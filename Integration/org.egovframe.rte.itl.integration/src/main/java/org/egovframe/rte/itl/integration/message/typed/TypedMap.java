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
package org.egovframe.rte.itl.integration.message.typed;

import org.egovframe.rte.itl.integration.type.NoSuchRecordFieldException;
import org.egovframe.rte.itl.integration.type.RecordType;
import org.egovframe.rte.itl.integration.type.Type;
import org.egovframe.rte.itl.integration.type.UnassignableValueException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 연계 서비스의 표준 메시지 구현 클래스.
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지 Instance를 위한 Class이다. RecordType에 해당하는 Map
 * Instance를 나타낸다.
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
public class TypedMap implements Map<String, Object> {

	/** type */
	protected final RecordType type;

	/** inner map */
	protected Map<String, Object> inner = new HashMap<String, Object>();

	/**
	 * Argument <code>type</code>의 TypedMap instance를 생성한다.
	 * 
	 * @param type
	 *            type
	 * @throws IllegalArgumentException
	 *             Argument <code>type</code> 값이 <code>null</code>인 경우
	 */
	public TypedMap(final RecordType type) {
		super();
		if (type == null) {
			throw new IllegalArgumentException();
		}
		this.type = type;
	}

	/**
	 * Argument <code>type</code>의 TypedMap instance를 생성한다.
	 * 
	 * @param type
	 *            type
	 * @param map
	 *            map
	 * @throws IllegalArgumentException
	 *             Argument <code>type</code> 값이 <code>null</code>인 경우
	 */
	public TypedMap(final RecordType type, final Map<? extends String, ? extends Object> map) {
		this(type);
		putAll(map);
	}

	/**
	 * TypedMap의 RecordType을 읽어온다.
	 * 
	 * @return type
	 */
	public RecordType getType() {
		return type;
	}

	/**
	 * <code>fieldName</code>으로 정의된 Field의 Type을 읽어온다.
	 * 
	 * @param fieldName
	 *            field명
	 * @return field의 Type, 없을 경우 null
	 * @trhows NoSuchFieldException <code>fieldName</code>으로 정의된 Field가 없을 경우
	 */
	public Type getFieldType(final String fieldName) {
		return type.getFieldType(fieldName);
	}

	/**
	 * Argument <code>value</code>의 값을 Argument <code>key</code>의 이름으로 정의되어 있는
	 * field type에 맞게 변환한다.
	 * 
	 * @param key
	 *            field 명
	 * @param value
	 *            value
	 * @return 변환된 value
	 * @throws NoSuchRecordFieldException
	 *             <code>key</code>로 정의된 field가 없을 경우
	 * @throws UnassignableValueException
	 *             <code>key</code>로 정의된 Field에 <code>value</code> 값을 assign할 수
	 *             없는 경우
	 */
	protected Object convertToTypedObject(final String key, final Object value) {
		return getFieldType(key).convertToTypedObject(value);
	}

	/**
	 * Argument <code>map</code>에 저장되어 있는 값을, 각 field type에 맞게 변환한다.
	 * 
	 * @param map
	 *            값을 담고 있는 Map 객체
	 * @return 변환된 값을 담고 있는 Map 객체
	 * @throws NoSuchRecordFieldException
	 *             Argument <code>map</code>이 담고 있는 값들 중 해당하는 field가 없을 경우
	 * @throws UnassignableValueException
	 *             <code>map</code>이 담고 있는 값들 중 해당하는 field에 assign할 수 없는 경우
	 */
	protected Map<String, Object> convertToTypedObjects(final Map<? extends String, ? extends Object> map) {
		if (map == null) {
			return null;
		}
		Map<String, Object> newMap = new HashMap<String, Object>();
		for (Entry<? extends String, ? extends Object> entry : map.entrySet()) {
			newMap.put(entry.getKey(), convertToTypedObject(entry.getKey(), entry.getValue()));
		}
		return newMap;
	}

	public void clear() {
		inner.clear();
	}

	public boolean containsKey(Object arg0) {
		return inner.containsKey(arg0);
	}

	public boolean containsValue(Object arg0) {
		return inner.containsValue(arg0);
	}

	public Set<Entry<String, Object>> entrySet() {
		return inner.entrySet();
	}

	public Object get(Object arg0) {
		getFieldType((String) arg0);
		return inner.get(arg0);
	}

	public boolean isEmpty() {
		return inner.isEmpty();
	}

	public Set<String> keySet() {
		return inner.keySet();
	}

	public Object put(String arg0, Object arg1) {
		return inner.put(arg0, convertToTypedObject(arg0, arg1));
	}

	public void putAll(Map<? extends String, ? extends Object> arg0) {
		inner.putAll(convertToTypedObjects(arg0));
	}

	public Object remove(Object arg0) {
		getFieldType((String) arg0);
		return inner.remove(arg0);
	}

	public int size() {
		return inner.size();
	}

	public Collection<Object> values() {
		return inner.values();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inner == null) ? 0 : inner.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TypedMap other = (TypedMap) obj;
		if (inner == null) {
			if (other.inner != null) {
				return false;
			}
		} else if (!inner.equals(other.inner)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

}
