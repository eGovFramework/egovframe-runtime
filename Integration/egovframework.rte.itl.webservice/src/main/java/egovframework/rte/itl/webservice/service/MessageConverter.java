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
package egovframework.rte.itl.webservice.service;

import egovframework.rte.itl.integration.type.Type;

/**
 * Map, List 등으로 구성된 메시지와 VO 간의 변환을 수행하는 인터페이스
 * <p>
 * <b>NOTE:</b> Map, List 등으로 구성된 메시지와 Value Object 간의 변환을 수행하는 interface이다. </p>
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
public interface MessageConverter {
	/**
	 * Map, List 등으로 구성된 메시지를 Value Object로 변환한다.
	 * 
	 * @param source
	 *            원본 메시지
	 * @param type
	 *            변환할 Value Object에 해당하는 Type
	 * @return 변환한 Value Object
	 * @throws ClassNotFoundException
	 *             Value Object의 class를 생성할 수 없는 경우
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 */
	public Object convertToValueObject(Object source, Type type)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, NoSuchFieldException;

	/**
	 * Value Object를 Map/List로 구성된 메시지로 변환한다.
	 * 
	 * @param source
	 *            원본 Value Object
	 * @param type
	 *            Value Object에 해당하는 type
	 * @return Map/List 메시지
	 * @throws ClassNotFoundException
	 *             Value Object의 class를 생성할 수 없는 경우
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws InstantiationException
	 */
	public Object convertToTypedObject(Object source, Type type)
			throws ClassNotFoundException, IllegalAccessException,
			NoSuchFieldException, InstantiationException;
}
