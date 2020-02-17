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
package egovframework.rte.itl.integration.metadata.dao;

import java.util.List;

import egovframework.rte.itl.integration.metadata.IntegrationDefinition;

/**
 * 연계 서비스 메타 데이터의 IntegrationDefinition을 읽어오기 위한 DAO 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 메타 데이터의 IntegrationDefinition을 읽어오기 위한 Data Access
 * Object interface이다.
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
public interface IntegrationDefinitionDao {

	/**
	 * 연계 primary key (<code>id</code>)에 해당하는 IntegrationDefinition을 읽어온다.
	 * 
	 * @param id
	 *            연계 ID
	 * @return IntegrationDefinition, 해당하는 값이 없를 경우 <code>null</code>
	 */
	public IntegrationDefinition getIntegrationDefinition(final String id);

	/**
	 * 시스템의 alternative key ( <code>consumerOrganizationId</code>,
	 * <code>consumerSystemId</code>)이 요청 시스템으로 등록되어 있는 IntegrationDefinition을
	 * 읽어온다.
	 * 
	 * @param consumerOrganizationId
	 *            요청 기관 ID
	 * @param consumerSystemId
	 *            요청 시스템 ID
	 * @return list of IntegrationDefinition
	 */
	public List<IntegrationDefinition> getIntegrationDefinitionOfConsumer(final String consumerOrganizationId, final String consumerSystemId);

	/**
	 * 시스템의 alternative key ( <code>providerOrganizationId</code>,
	 * <code>providerSystemId</code>)이 제공 시스템으로 등록되어 있는 IntegrationDefinition을
	 * 읽어온다.
	 * 
	 * @param providerOrganizationId
	 *            제공 기관 ID
	 * @param providerSystemId
	 *            제공 시스템 ID
	 * @return list of IntegrationDefinition
	 */
	public List<IntegrationDefinition> getIntegrationDefinitionOfProvider(final String providerOrganizationId, final String providerSystemId);
}
