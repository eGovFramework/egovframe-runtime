/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.access.config;

import javax.sql.DataSource;

/**
 * egov-access schema namespace 처리를 담당하는 bean 클래스
 *
 * <p>Desc.: 설정 간소화 처리에 사용되는 bean으로 설정에 대한 정보를 보관</p>
 *
 * @author ESFC
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2019.10.01	ESFC            최초 생성
 * 2019.12.30   신용호            mappingPath 추가
 * </pre>
 */
public class EgovAccessConfig {

    private String globalAuthen;
    private DataSource dataSource;
    private String loginUrl;
    private String accessDeniedUrl;
    private String sqlAuthorityUser;
    private String sqlRoleAndUrl;
    private String requestMatcherType;
    private String excludeList;
	private String mappingPath;

    public String getGlobalAuthen() {
        return globalAuthen;
    }

    public void setGlobalAuthen(String globalAuthen) {
        this.globalAuthen = globalAuthen;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getAccessDeniedUrl() {
        return accessDeniedUrl;
    }

    public void setAccessDeniedUrl(String accessDeniedUrl) {
        this.accessDeniedUrl = accessDeniedUrl;
    }

    public String getSqlAuthorityUser() {
        return sqlAuthorityUser;
    }

    public void setSqlAuthorityUser(String sqlAuthorityUser) {
        this.sqlAuthorityUser = sqlAuthorityUser;
    }

    public String getSqlRoleAndUrl() {
        return sqlRoleAndUrl;
    }

    public void setSqlRoleAndUrl(String sqlRoleAndUrl) {
        this.sqlRoleAndUrl = sqlRoleAndUrl;
    }

    public String getRequestMatcherType() {
        return requestMatcherType;
    }

    public void setRequestMatcherType(String requestMatcherType) {
        this.requestMatcherType = requestMatcherType;
    }

    public String getExcludeList() {
        return excludeList;
    }

    public void setExcludeList(String excludeList) {
        this.excludeList = excludeList;
    }

    public String getMappingPath() {
		return mappingPath;
	}

	public void setMappingPath(String mappingPath) {
		this.mappingPath = mappingPath;
	}

}
