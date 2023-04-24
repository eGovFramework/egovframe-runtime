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

import org.egovframe.rte.itl.integration.type.ListType;
import org.egovframe.rte.itl.integration.type.PrimitiveType;
import org.egovframe.rte.itl.integration.type.RecordType;
import org.egovframe.rte.itl.integration.type.Type;
import org.egovframe.rte.itl.webservice.service.EgovWebServiceClassLoader;
import org.egovframe.rte.itl.webservice.service.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

/**
 * Map, List 등으로 구성된 메시지와 Value Object 간의 변환을 수행하는 구현 클래스
 * <p>
 * <b>NOTE:</b> Map, List 등으로 구성된 메시지와 Value Object 간의 변환을 수행하는 class이다. </p>
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
public class MessageConverterImpl implements MessageConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageConverterImpl.class);

	/** EgovWebServiceClassLoader */
	private EgovWebServiceClassLoader classLoader;

	/**
	 * Constructor
	 * 
	 * @param classLoader
	 *            EgovWebServiceClassLoader
	 * @throws IllegalArgumentException
	 *             <code>classLoader</code> 값이 <code>null</code>인 경우
	 */
	public MessageConverterImpl(EgovWebServiceClassLoader classLoader) {
		super();
		if (classLoader == null) {
			throw new IllegalArgumentException();
		}
		this.classLoader = classLoader;
	}

	@SuppressWarnings("unchecked")
	public Object convertToValueObject(Object source, Type type) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
		LOGGER.debug("convertToValueObject(source = {}, type = {})", source, type);
		if (type instanceof PrimitiveType) {
			LOGGER.debug("Type is a Primitive Type");
			return source;
		} else if (type instanceof ListType) {
			LOGGER.debug("Type is a List Type");
			ListType listType = (ListType) type;
			Object[] components = ((Collection<?>) source).toArray();
			Class<?> arrayClass = classLoader.loadClass(listType);
			Object array = Array.newInstance(arrayClass.getComponentType(), components.length);
			for (int i = 0; i < components.length; i++) {
				Array.set(array, i, convertToValueObject(components[i], listType.getElementType()));
			}
			return array;
		} else if (type instanceof RecordType) {
			LOGGER.debug("Type is a Record(Map) Type");
			RecordType recordType = (RecordType) type;
			Map<String, Object> map = (Map<String, Object>) source;
			Class<?> recordClass = classLoader.loadClass(recordType);
			Object record = recordClass.newInstance();
			for (Entry<String, Object> entry : map.entrySet()) {
				Object fieldValue = convertToValueObject(entry.getValue(), recordType.getFieldType(entry.getKey()));
				recordClass.getField(entry.getKey()).set(record, fieldValue);
			}
			return record;
		}
		LOGGER.error("Type is invalid");
		throw new InstantiationException();
	}

	public Object convertToTypedObject(Object source, Type type) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException, InstantiationException {
		LOGGER.debug("convertToTypedObject(source = {}, type = {})", source, type);
		if (type instanceof PrimitiveType) {
			LOGGER.debug("Type is a Primitive Type");
			return source;
		} else if (type instanceof ListType) {
			LOGGER.debug("Type is a List Type");
			ListType listType = (ListType) type;
			Object[] components = (Object[]) source;
			List<Object> list = new ArrayList<Object>();
			for (Object component : components) {
				list.add(convertToTypedObject(component, listType.getElementType()));
			}
			return list;
		} else if (type instanceof RecordType) {
			LOGGER.debug("Type is a Record(Map) Type");
			RecordType recordType = (RecordType) type;
			Class<?> recordClass = classLoader.loadClass(recordType);
			Map<String, Object> map = new HashMap<String, Object>();
			for (Entry<String, Type> entry : recordType.getFieldTypes() .entrySet()) {
				Object fieldValue = recordClass.getField(entry.getKey()).get(source);
				map.put(entry.getKey(), convertToTypedObject(fieldValue, entry.getValue()));
			}
			return map;
		}
		LOGGER.error("Type is invalid");
		throw new InstantiationException();
	}

}
