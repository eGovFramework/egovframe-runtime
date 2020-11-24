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
package org.egovframe.rte.itl.integration.type.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.egovframe.rte.itl.integration.metadata.RecordTypeDefinition;
import org.egovframe.rte.itl.integration.metadata.RecordTypeFieldDefinition;
import org.egovframe.rte.itl.integration.metadata.dao.RecordTypeDefinitionDao;
import org.egovframe.rte.itl.integration.type.CircularInheritanceException;
import org.egovframe.rte.itl.integration.type.ListType;
import org.egovframe.rte.itl.integration.type.NoSuchTypeException;
import org.egovframe.rte.itl.integration.type.PrimitiveType;
import org.egovframe.rte.itl.integration.type.RecordType;
import org.egovframe.rte.itl.integration.type.Type;
import org.egovframe.rte.itl.integration.type.TypeLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 전자정부 연계 서비스의 표준 메시지의 Type을 읽어오기 위한 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지의 Type을 읽어오기 위한 Class이다.
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
public class TypeLoaderUsingMetadata implements TypeLoader, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypeLoaderUsingMetadata.class);

	/**
	 * <code>RecordTypeDefinition</code>를 읽어오기 위한 DAO 객체
	 */
	private RecordTypeDefinitionDao recordTypeDefinitionDao;

	/** 기존에 load된 Type을 담고 있는 map */
	private Map<String, Type> typePool = new HashMap<String, Type>();

	/**
	 * Default Constructor
	 */
	public TypeLoaderUsingMetadata() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param recordTypeDefinitionDao
	 */
	public TypeLoaderUsingMetadata(
			final RecordTypeDefinitionDao recordTypeDefinitionDao) {
		super();
		this.recordTypeDefinitionDao = recordTypeDefinitionDao;
	}

	/**
	 * recordTypeDefinitionDao
	 * 
	 * @param recordTypeDefinitionDao
	 *            the recordTypeDefinitionDao to set
	 */
	public void setRecordTypeDefinitionDao(
			RecordTypeDefinitionDao recordTypeDefinitionDao) {
		this.recordTypeDefinitionDao = recordTypeDefinitionDao;
	}

	public void afterPropertiesSet() throws Exception {
		if (recordTypeDefinitionDao == null) {
			throw new IllegalArgumentException();
		}
	}

	public Type getType(String id) {
		return getType(id, null);
	}

	/**
	 * Type <code>id</code>에 해당하는 <code>Type</code> 객체를 읽어온다.
	 * <code>loadingTypes</code>는 현재 loading하고 있는 Type 객제로서 circular reference가
	 * 발생한 경우, 무한 loop에 빠지는 것을 막기 위한 arument이다.
	 * 
	 * @param id
	 *            type id
	 * @param loadingTypes
	 *            loading중인 Types
	 * @return <code>Type</code> 객체
	 * @throws NoSuchTypeException
	 *             <code>id</code>가 null이거나, 해당하는 Type이 정의되어 있지 않을 경우
	 * @throws CircularInheritanceException
	 *             RecordType의 경우, 순환 상속이 발생한 경우
	 */
	private Type getType(String id, Map<String, Type> loadingTypes) {
		LOGGER.debug("get Type(id = \"{}\")", id);

		// id 값이 null인 경우, NoSuchTypeException을 던진다.
		if (id == null) {
			LOGGER.error("Argument 'id' is null");
			throw new NoSuchTypeException();
		}

		Type type = null;

		// 현재 loading중인 Type 중에서 찾는다.
		if (loadingTypes != null) {
			type = loadingTypes.get(id);
			if (type != null) {
				LOGGER.debug("Type(id = \"{}\") exists in load hierachy : {}",
						id, type);
				return type;
			}
		}

		// Primitive Type 중에서 찾는다.
		type = PrimitiveType.getPrimitiveType(id);
		if (type != null) {
			LOGGER.debug("Type(id = \"{}\") is a primitive type : {}", id, type);
			return type;
		}

		// 기존의 load된 type 중에서 동일한 id의 Type을 검색한다.
		type = typePool.get(id);
		if (type != null) {
			LOGGER.debug("Type(id = \"{}\") is already loaded : {}", id, type);
			return type;
		}

		// [!!! 중요 !!!]
		// List Type의 Element Type 또는 Record Type의
		// Field Type이
		// 현재 id의 Type일 수 있다. 이 경우, 무한 loop에 빠질 수 있으므로
		// 미리 객체를 생성하여 loadingTypes에 추가한 후, 내부 정의는 나중에
		// 추가한다.
		LOGGER.debug("Load new type(id= \"{}\"", id);
		if (loadingTypes == null) {
			loadingTypes = new HashMap<String, Type>();
		}

		// List Type 인지 검사한다.
		if (id.endsWith("[]")) {
			LOGGER.debug("Type(id = \"{}\") is a list type", id);
			// List Type (임시로 BOOLEAN Type을 Element로
			// 갖도록 생성한다.)
			type = new ListType(id, id, PrimitiveType.BOOLEAN); // 임시
			loadingTypes.put(id, type);

			Type elementType = getType(id.substring(0, id.length() - 2),
					loadingTypes);
			LOGGER.debug("ListType(id = \"{}\")'s elementType = {}", id,
					elementType);
			((ListType) type).setElementType(elementType);

			loadingTypes.remove(id);
		} else {
			LOGGER.debug("Type(id = \"{}\") is a record type", id);
			RecordTypeDefinition recordTypeDefinition = recordTypeDefinitionDao.getRecordTypeDefinition(id);
			if (recordTypeDefinition == null) {
				LOGGER.error("No Such RecordTypeDefinition(id = \"{}\"", id);
				throw new NoSuchTypeException();
			}
			if (recordTypeDefinition.isValid() == false) {
				LOGGER.error("RecordTypeDefinition(id = \"{}\" is invalid", id);
				throw new NoSuchTypeException();
			}

			// Record Type
			type = new RecordType(id, recordTypeDefinition.getName());
			loadingTypes.put(id, type);

			Map<String, RecordTypeDefinition> occurredTypes = new HashMap<String, RecordTypeDefinition>();
			Map<String, Type> fieldTypes = new HashMap<String, Type>();
			RecordTypeDefinition currentRecordTypeDefinition = recordTypeDefinition;
			while (currentRecordTypeDefinition != null) {
				if (occurredTypes.containsKey(currentRecordTypeDefinition.getId())) {
					throw new CircularInheritanceException();
				}
				occurredTypes.put(currentRecordTypeDefinition.getId(),
						currentRecordTypeDefinition);

				for (Entry<String, RecordTypeFieldDefinition> entry : currentRecordTypeDefinition.getFields().entrySet()) {
					if (fieldTypes.containsKey(entry.getKey()) == false) {
						fieldTypes.put(
								entry.getKey(),
								getType(entry.getValue().getTypeId(),
										loadingTypes));
					}
				}
				currentRecordTypeDefinition = currentRecordTypeDefinition.getParent();
			}
			if (LOGGER.isDebugEnabled()) {
				for (Entry<String, Type> entry : fieldTypes.entrySet()) {
					LOGGER.debug(
							"RecordType(id = \"{}\")'s field[{}] type = {}",
							id, entry.getKey(), entry.getValue());
				}
			}
			((RecordType) type).setFieldTypes(fieldTypes);

			loadingTypes.remove(id);
		}

		typePool.put(id, type);

		return type;
	}

}
