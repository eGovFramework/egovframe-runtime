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

import org.egovframe.rte.itl.integration.util.Validatable;

/**
 * 전자정부 연계 서비스의 표준 메시지의 Type을 정의하기 위한 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지의 Type을 정의하기 위한 Interface이다.
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
public interface Type extends Validatable {

	/**
	 * Id
	 * 
	 * @return the id
	 */
	public String getId();

	/**
	 * name
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * <code>source</code> 값을 assign할 수 있는 형태로 변환한다.
	 * 
	 * @param source
	 *            source value
	 * @return assign할 수 있는 형태의 값
	 * @throws UnassignableValueException
	 *             Assign할 수 없는 값인 경우
	 */
	public Object convertToTypedObject(final Object source);

	/**
	 * <code>source</code> 값이 assign할 수 있는지 알려준다.
	 * 
	 * @param source
	 *            source
	 * @return assign할 수 있으면 true, 없으면 false
	 */
	public boolean isAssignableValue(final Object source);

	/**
	 * Argument <code>clazz</code> 형식의 값을 assign할 수 있는지 알려준다.
	 * 
	 * @param clazz
	 *            assign할 Class
	 * @return assign할 수 있으면 true, 그렇지 않으면 false
	 * @throws IllegalArgumentException
	 *             <code>clazz</code> 값이 <code>null</code> 인 경우
	 */
	public boolean isAssignableFrom(final Class<?> clazz);

}
