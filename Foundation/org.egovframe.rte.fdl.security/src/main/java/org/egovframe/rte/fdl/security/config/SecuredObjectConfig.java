/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
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
package org.egovframe.rte.fdl.security.config;


/**
 * egov-security schema namespace 처리를 담당하는 bean 클래스
 * 
 *<p>Desc.: 설정 간소화 처리에 사용되는 bean으로 설정에 대한 정보를 보관</p>
 *
 * @author Vincent Han
 * @since 2014.03.12
 * @version 3.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.03.12	한성곤				Spring Security 설정 간소화 기능 추가
 * </pre>
 */
public class SecuredObjectConfig {
    
    private String sqlRolesAndUrl;
    private String sqlRolesAndMethod;
    private String sqlRolesAndPointcut;
    private String sqlRegexMatchedRequestMapping;
    private String sqlHierarchicalRoles;
	private String roleHierarchyString;

	public String getSqlRolesAndUrl() {
		return sqlRolesAndUrl;
	}

	public void setSqlRolesAndUrl(String sqlRolesAndUrl) {
		this.sqlRolesAndUrl = sqlRolesAndUrl;
	}

	public String getSqlRolesAndMethod() {
		return sqlRolesAndMethod;
	}

	public void setSqlRolesAndMethod(String sqlRolesAndMethod) {
		this.sqlRolesAndMethod = sqlRolesAndMethod;
	}

	public String getSqlRolesAndPointcut() {
		return sqlRolesAndPointcut;
	}

	public void setSqlRolesAndPointcut(String sqlRolesAndPointcut) {
		this.sqlRolesAndPointcut = sqlRolesAndPointcut;
	}

	public String getSqlRegexMatchedRequestMapping() {
		return sqlRegexMatchedRequestMapping;
	}

	public void setSqlRegexMatchedRequestMapping(String sqlRegexMatchedRequestMapping) {
		this.sqlRegexMatchedRequestMapping = sqlRegexMatchedRequestMapping;
	}

	public String getSqlHierarchicalRoles() {
		return sqlHierarchicalRoles;
	}

	public void setSqlHierarchicalRoles(String sqlHierarchicalRoles) {
		this.sqlHierarchicalRoles = sqlHierarchicalRoles;
	}

	public String getRoleHierarchyString() {
		return roleHierarchyString;
	}

	public void setRoleHierarchyString(String roleHierarchyString) {
		this.roleHierarchyString = roleHierarchyString;
	}

}
