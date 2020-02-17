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
package egovframework.rte.itl.integration.type;

import java.util.Collection;

import egovframework.rte.itl.integration.message.typed.TypedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 전자정부 연계 서비스의 표준 메시지를 정의 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지를 정의하기 위한 Class이다. Colleciton Type 중 List
 * Type을 정의하기 위한 Class이다. List Type은 한 종류의 Type만을 담을 수 있다.
 * </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 * 
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.06.01  심상호           최초 생성
 *
 * </pre>
 */
public class ListType extends AbstractType {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ListType.class);

	// CHECKSTYLE:OFF
	/** ListType에 Assign할 수 있는 Java 객체 Classes */
	protected static final Class<?>[] listTypeAssignableClasses = new Class<?>[] {
			TypedList.class, Collection.class };
	// CHECKSTYLE:ON
	/** 담을 수 있는 Element의 Type */
	protected Type elementType = null;

	/**
	 * Default Constructor
	 */
	public ListType() {
		super();
	}

	/**
	 * <code>id</code>와 <code>elementType</code>를 가진 ListType을 생성한다.
	 * 
	 * @param id
	 *            Type Id
	 * @param name
	 *            Type Name
	 * @param elementType
	 *            elementType
	 * @throws IllegalArgumentException
	 *             1. Argument <code>id</code> 값이 <code>null</code>이거나, 공백 문자로만
	 *             이루어진 경우 2. Argument <code>name</code> 값이 <code>null</code>
	 *             이거나, 공백 문자로만 이루어진 경우 3. <code>elementType</code> 값이
	 *             <code>null</code>인 경우
	 */
	public ListType(final String id, final String name, final Type elementType) {
		super(id, name, listTypeAssignableClasses);
		if (elementType == null) {
			LOGGER.error("Argument 'elementType' is null");
			throw new IllegalArgumentException();
		}
		this.elementType = elementType;
	}

	/**
	 * elementType
	 * 
	 * @return the elementType
	 */
	public Type getElementType() {
		return elementType;
	}

	/**
	 * TypeLoader에서만 사용된다.
	 * 
	 * @param elementType
	 *            the elementType to set
	 * @throws Argument
	 *             <code>elementType</code> 값이 null인 경우
	 */
	public void setElementType(Type elementType) {
		if (elementType == null) {
			LOGGER.error("Argument 'elementType' is null");
			throw new IllegalArgumentException();
		}
		this.elementType = elementType;
	}

	@Override
	public boolean isAssignableFrom(Class<?> clazz) {
		if (super.isAssignableFrom(clazz)) {
			return true;
		}
		if (clazz.isArray()) {
			return elementType.isAssignableFrom(clazz.getComponentType());
		}
		return false;
	}

	public Object convertToTypedObject(final Object source) {
		if (source == null) {
			return null;
		}
		if (source instanceof TypedList) {
			if (this.equals(((TypedList) source).getType()) == false) {
				throw new UnassignableValueException();
			}
			return source;
		}
		if (source instanceof Collection) {
			return new TypedList(this, (Collection<?>) source);
		}
		if (source.getClass().isArray()) {
			return new TypedList(this, source);
		}
		throw new UnassignableValueException();
	}

	public boolean isAssignableValue(Object source) {
		if (source == null) {
			return true;
		}
		if (source instanceof TypedList) {
			return this.equals(((TypedList) source).getType());
		}
		if (source instanceof Collection) {
			for (Object sourceElement : (Iterable<?>) source) {
				if (elementType.isAssignableValue(sourceElement) == false) {
					return false;
				}
			}
			return true;
		}
		if (source.getClass().isArray()) {
			return elementType.isAssignableFrom(source.getClass().getComponentType());
		}
		return false;
	}

	@Override
	public boolean isValid() {
		return (super.isValid() && elementType != null && elementType.isValid());
	}
}
