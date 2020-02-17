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
 * 웹 서비스에 필요한 클래스를 생성하는 서비스의 인터페이스
 * <p>
 * <b>NOTE:</b> 웹 서비스에 필요한 Type, ServiceEndpointInterface, ServiceEndpoint 등의
 * class를 생성하는 ClassLoader interface이다. </p>
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
public interface EgovWebServiceClassLoader {

	/**
	 * ServiceInterface class의 ServiceBridge field name을 return한다.
	 * 
	 * @return field name of ServiceBridge
	 */
	public String getFieldNameOfServiceBridge();

	/**
	 * <code>type</code>에 해당하는 Java Class를 load한다.
	 * 
	 * @param type
	 *            type
	 * @return Class 객체
	 * @throws ClassNotFoundException
	 *             <code>type</code>에 해당하는 Class를 생성할 수 없는 경우
	 */
	public Class<?> loadClass(final Type type) throws ClassNotFoundException;

	/**
	 * <code>serviceEndpointInfo</code>에 해당하는 Service Endpoint class를 load한다.
	 * Service Endpoint Interface 및 Request/Response Message Type 역시 같이 load한다.
	 * 
	 * @param serviceEndpointInfo
	 *            serviceEndpointInfo
	 * @return class of Service Endpoint Implementation
	 * @throws ClassNotFoundException
	 *             class를 생성할 수 없을 경우
	 */
	public Class<?> loadClass(final ServiceEndpointInfo serviceEndpointInfo)
			throws ClassNotFoundException;

	/**
	 * <code>serviceEndpointInterfaceInfo</code>에 해당하는 Service Endpoint
	 * Interface class를 load한다.
	 * 
	 * @param serviceEndpointInterfaceInfo
	 *            serviceEndpointInterfaceInfo
	 * @return class of Service Endpoint Interface
	 * @throws ClassNotFoundException
	 *             class를 생성할 수 없는 경우
	 */
	public Class<?> loadClass(
			final ServiceEndpointInterfaceInfo serviceEndpointInterfaceInfo)
			throws ClassNotFoundException;
}
