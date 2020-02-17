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

/**
 * 전자정부 연계 서비스의 표준 메시지의 Type을 읽어오기 위한 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지의 Type을 읽어오기 위한 interface이다.
 * </p>
 * 
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
public interface TypeLoader {

	/**
	 * Type <code>id</code>에 해당하는 <code>Type</code> 객체를 읽어온다.
	 * 
	 * @param id
	 *            type id
	 * @return <code>Type</code> 객체
	 * @throws NoSuchTypeException
	 *             <code>id</code>가 null이거나, 해당하는 Type이 정의되어 있지 않을 경우
	 * @throws CircularInheritanceException
	 *             RecordType의 경우, 순환 상속이 발생한 경우
	 */
	public Type getType(final String typeId);

}
