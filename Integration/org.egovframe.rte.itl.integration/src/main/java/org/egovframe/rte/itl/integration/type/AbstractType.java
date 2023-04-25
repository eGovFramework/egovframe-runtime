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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 연계 서비스의 표준 메시지의 Type을 정의하기 위한 기본 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지의 Type을 정의하기 위한 기본 Class이다. 메시지를 형식(Type)을
 * 정의하는 Class 의 최상위 Class 이다.
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
public abstract class AbstractType implements Type {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractType.class);

	/** Type Id */
	protected String id;

	/** Type Name */
	protected String name;

	/** Assign 가능한 Java Class들 */
	protected List<Class<?>> assignableClasses;

	/**
	 * Default Constructor
	 */
	protected AbstractType() {
		super();
	}

	/**
	 * Argument <code>id</code>의 Type을 생성한다.
	 * 
	 * @param id
	 *            Type Id
	 * @param name
	 *            Type Name
	 * @param assignableClasses
	 *            assign할 수 있는 Java 객체의 Class들
	 * @throws IllegalArgumentException
	 *             1. Argument <code>id</code> 값이 <code>null</code>이거나, 공백 문자로만
	 *             이루어진 경우 2. Argument <code>name</code> 값이 <code>null</code>
	 *             이거나, 공백 문자로만 이루어진 경우 3. Argument
	 *             <code>assignableClasses</code>가 <code>null</code>이거나 빈 Array인
	 *             경우
	 */
	protected AbstractType(final String id, final String name,
			final Class<?>[] assignableClasses) {
		this(id, name, Arrays.asList(assignableClasses));
	}

	/**
	 * Argument <code>id</code>의 Type을 생성한다.
	 * 
	 * @param id
	 *            Type Id
	 * @param name
	 *            Type Name
	 * @param assignableClasses
	 *            assign할 수 있는 Java 객체의 Class들
	 * @throws IllegalArgumentException
	 *             1. Argument <code>id</code> 값이 <code>null</code>이거나, 공백 문자로만
	 *             이루어진 경우 2. Argument <code>name</code> 값이 <code>null</code>
	 *             이거나, 공백 문자로만 이루어진 경우 3. Argument
	 *             <code>assignableClasses</code>가 <code>null</code>이거나 빈 List인
	 *             경우
	 */
	protected AbstractType(final String id, final String name,
			final List<Class<?>> assignableClasses) {
		super();
		if (StringUtils.hasText(id) == false) {
			LOGGER.error("id does not have a text (id = \"{}\")", id);
			throw new IllegalArgumentException();
		} else if (StringUtils.hasText(name) == false) {
			LOGGER.error("name does not have a text (name = \"{}\")", name);
			throw new IllegalArgumentException();
		} else if (assignableClasses == null || assignableClasses.size() == 0) {
			LOGGER.error("assignableClasses is null or empty (assignableClasses = {})", assignableClasses);
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.name = name;
		this.assignableClasses = assignableClasses;
	}

	public String getId() {
		return id;
	}

	/**
	 * Id
	 * 
	 * @param id
	 *            the id to set
	 * @throws IllegalArgumentException
	 *             Argument <code>id</code> 값이 <code>null</code>이거나, 공백 문자로만
	 *             이루어진 경우
	 */
	public void setId(String id) {
		if (StringUtils.hasText(id) == false) {
			LOGGER.error("id does not have a text (id = \"{}\")", id);
			throw new IllegalArgumentException();
		}
		this.id = id;
	}

	public String getName() {
		return name;
	}

	/**
	 * name
	 * 
	 * @param name
	 *            the name to set
	 * @throws IllegalArgumentException
	 *             Argument <code>name</code> 값이 <code>null</code>이거나, 공백 문자로만
	 *             이루어진 경우
	 */
	public void setName(String name) {
		if (StringUtils.hasText(name) == false) {
			LOGGER.error("name does not have a text (name = \"{}\")", name);
			throw new IllegalArgumentException();
		}
		this.name = name;
	}

	/**
	 * assignableClasses
	 * 
	 * @return the assignableClasses
	 */
	public List<Class<?>> getAssignableClasses() {
		return assignableClasses;
	}

	/**
	 * assignableClasses
	 * 
	 * @param assignableClasses
	 *            the assignableClasses to set
	 * @throws IllegalArgumentException
	 *             Argument <code>assignableClasses</code> 가 <code>null</code>
	 *             이거나 빈 Array인 경우
	 */
	public void setAssignableClasses(List<Class<?>> assignableClasses) {
		if (assignableClasses == null || assignableClasses.size() == 0) {
			LOGGER.error("assignableClasses is null or empty (assignableClasses = {})", assignableClasses);
			throw new IllegalArgumentException();
		}
		this.assignableClasses = assignableClasses;
	}

	public boolean isAssignableFrom(final Class<?> clazz) {
		if (clazz == null) {
			LOGGER.error("Argument 'clazz' is null");
			throw new IllegalArgumentException();
		}
		for (Class<?> assignableClass : assignableClasses) {
			if (assignableClass.isAssignableFrom(clazz)) {
				return true;
			}
		}
		return false;
	}

	public boolean isValid() {
		return (StringUtils.hasText(id) && StringUtils.hasText(name) && assignableClasses != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		AbstractType other = (AbstractType) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
				
		} else if (!id.equals(other.id)) {
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " {id = " + StringUtils.quote(id) + "}";
	}

}
