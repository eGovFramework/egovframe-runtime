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
package org.egovframe.rte.itl.webservice.service;

import java.util.Collection;

/**
 * 웹 서비스 ServiceEndpointInterface 정보를 나타내는 인터페이스
 * <p>
 * <b>NOTE:</b> 웹 서비스 ServiceEndpointInterface 정보를 나타내는 interface이다.
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
public interface ServiceEndpointInterfaceInfo {

	/**
	 * namespace
	 * 
	 * @return the namespace
	 */
	public String getNamespace();

	/**
	 * wsdlAddress
	 * 
	 * @return the wsdlAddress
	 */
	public String getWsdlAddress();

	/**
	 * serviceName
	 * 
	 * @return the serviceName
	 */
	public String getServiceName();

	/**
	 * portName
	 * 
	 * @return the portName
	 */
	public String getPortName();

	/**
	 * operationName
	 * 
	 * @return the operationName
	 */
	public String getOperationName();

	/**
	 * returnInfo
	 * 
	 * @return the returnInfo
	 */
	public ServiceParamInfo getReturnInfo();

	/**
	 * paramInfos
	 * 
	 * @return the paramInfos
	 */
	public Collection<ServiceParamInfo> getParamInfos();

}
