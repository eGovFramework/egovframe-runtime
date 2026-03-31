/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * egov-security schema namespace 처리를 담당하는 bean 클래스
 *
 * <p>Desc.: 설정 간소화 처리에 사용되는 bean으로 설정에 대한 정보를 보관</p>
 *
 * @author Vincent Han
 * @version 3.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.03.12	한성곤			Spring Security 설정 간소화 기능 추가
 * 2017.07.03	장동한			Spring Security 4.x 업그레이드(보안설정기능) 추가
 * 2020.05.27	유지보수			CSRF Access Denied 처리 URL 추가
 * 2023.08.31	유지보수			Spring 표현 언어(SpEL) 설정 옵션 추가
 * 2024.12.19   유지보수          properties 설정 지원 및 통합
 * </pre>
 * @since 2014.03.12
 */
public class EgovSecurityConfig {

    @JsonProperty("id")
    private String id;

    @JsonProperty("loginUrl")
    private String loginUrl;

    @JsonProperty("loginProcessUrl")
    private String loginProcessUrl;

    @JsonProperty("loginFailureUrl")
    private String loginFailureUrl;

    @JsonProperty("logoutUrl")
    private String logoutUrl;

    @JsonProperty("logoutSuccessUrl")
    private String logoutSuccessUrl;

    @JsonProperty("accessDeniedUrl")
    private String accessDeniedUrl;

    @JsonProperty("dataSource")
    private String dataSource;

    @JsonProperty("jdbcUsersByUsernameQuery")
    private String jdbcUsersByUsernameQuery;

    @JsonProperty("jdbcAuthoritiesByUsernameQuery")
    private String jdbcAuthoritiesByUsernameQuery;

    @JsonProperty("jdbcMapClass")
    private String jdbcMapClass;

    @JsonProperty("requestMatcherType")
    private String requestMatcherType;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("hashBase64")
    private boolean hashBase64;

    @JsonProperty("concurrentMaxSessons")
    private int concurrentMaxSessons;

    @JsonProperty("concurrentExpiredUrl")
    private String concurrentExpiredUrl;

    @JsonProperty("errorIfMaximumExceeded")
    private boolean errorIfMaximumExceeded;

    @JsonProperty("defaultTargetUrl")
    private String defaultTargetUrl;

    @JsonProperty("alwaysUseDefaultTargetUrl")
    private boolean alwaysUseDefaultTargetUrl;

    @JsonProperty("sniff")
    private boolean sniff;

    @JsonProperty("xframeOptions")
    private String xframeOptions;

    @JsonProperty("xssProtection")
    private boolean xssProtection;

    @JsonProperty("cacheControl")
    private boolean cacheControl;

    @JsonProperty("csrf")
    private boolean csrf;

    @JsonProperty("csrfAccessDeniedUrl")
    private String csrfAccessDeniedUrl;

    @JsonProperty("permitAllList")
    private String permitAllList;

    @JsonProperty("sqlHierarchicalRoles")
    private String sqlHierarchicalRoles;

    @JsonProperty("sqlRolesAndUrl")
    private String sqlRolesAndUrl;

    @JsonProperty("sqlRegexMatchedRequestMapping")
    private String sqlRegexMatchedRequestMapping;

    public String getPermitAllList() {
        return permitAllList;
    }

    @JsonProperty("permitAllList")
    public void setPermitAllList(String permitAllList) {
        this.permitAllList = permitAllList;
    }

    @JsonIgnore
    public List<String> getPermitAllPatterns() {
        if (permitAllList == null || permitAllList.isBlank()) return List.of();
        return Arrays.stream(permitAllList.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLoginProcessUrl() {
        return loginProcessUrl;
    }

    public void setLoginProcessUrl(String loginProcessUrl) {
        this.loginProcessUrl = loginProcessUrl;
    }

    public String getLoginFailureUrl() {
        return loginFailureUrl;
    }

    public void setLoginFailureUrl(String loginFailureUrl) {
        this.loginFailureUrl = loginFailureUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getLogoutSuccessUrl() {
        return logoutSuccessUrl;
    }

    public void setLogoutSuccessUrl(String logoutSuccessUrl) {
        this.logoutSuccessUrl = logoutSuccessUrl;
    }

    public String getAccessDeniedUrl() {
        return accessDeniedUrl;
    }

    public void setAccessDeniedUrl(String accessDeniedUrl) {
        this.accessDeniedUrl = accessDeniedUrl;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getJdbcUsersByUsernameQuery() {
        return jdbcUsersByUsernameQuery;
    }

    public void setJdbcUsersByUsernameQuery(String jdbcUsersByUsernameQuery) {
        this.jdbcUsersByUsernameQuery = jdbcUsersByUsernameQuery;
    }

    public String getJdbcAuthoritiesByUsernameQuery() {
        return jdbcAuthoritiesByUsernameQuery;
    }

    public void setJdbcAuthoritiesByUsernameQuery(String jdbcAuthoritiesByUsernameQuery) {
        this.jdbcAuthoritiesByUsernameQuery = jdbcAuthoritiesByUsernameQuery;
    }

    public String getJdbcMapClass() {
        return jdbcMapClass;
    }

    public void setJdbcMapClass(String jdbcMapClass) {
        this.jdbcMapClass = jdbcMapClass;
    }

    public String getRequestMatcherType() {
        return requestMatcherType;
    }

    public void setRequestMatcherType(String requestMatcherType) {
        this.requestMatcherType = requestMatcherType;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isHashBase64() {
        return hashBase64;
    }

    public void setHashBase64(boolean hashBase64) {
        this.hashBase64 = hashBase64;
    }

    public int getConcurrentMaxSessons() {
        return concurrentMaxSessons;
    }

    public void setConcurrentMaxSessons(int concurrentMaxSessons) {
        this.concurrentMaxSessons = concurrentMaxSessons;
    }

    public String getConcurrentExpiredUrl() {
        return concurrentExpiredUrl;
    }

    public void setConcurrentExpiredUrl(String concurrentExpiredUrl) {
        this.concurrentExpiredUrl = concurrentExpiredUrl;
    }

    public boolean isErrorIfMaximumExceeded() {
        return errorIfMaximumExceeded;
    }

    public void setErrorIfMaximumExceeded(boolean errorIfMaximumExceeded) {
        this.errorIfMaximumExceeded = errorIfMaximumExceeded;
    }

    public String getDefaultTargetUrl() {
        return defaultTargetUrl;
    }

    public void setDefaultTargetUrl(String defaultTargetUrl) {
        this.defaultTargetUrl = defaultTargetUrl;
    }

    public boolean isAlwaysUseDefaultTargetUrl() {
        return alwaysUseDefaultTargetUrl;
    }

    public void setAlwaysUseDefaultTargetUrl(boolean alwaysUseDefaultTargetUrl) {
        this.alwaysUseDefaultTargetUrl = alwaysUseDefaultTargetUrl;
    }

    public boolean isSniff() {
        return sniff;
    }

    public void setSniff(boolean sniff) {
        this.sniff = sniff;
    }

    public String getXframeOptions() {
        return xframeOptions;
    }

    public void setXframeOptions(String xframeOptions) {
        this.xframeOptions = xframeOptions;
    }

    public boolean isXssProtection() {
        return xssProtection;
    }

    public void setXssProtection(boolean xssProtection) {
        this.xssProtection = xssProtection;
    }

    public boolean isCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(boolean cacheControl) {
        this.cacheControl = cacheControl;
    }

    public boolean isCsrf() {
        return csrf;
    }

    public void setCsrf(boolean csrf) {
        this.csrf = csrf;
    }

    public String getCsrfAccessDeniedUrl() {
        return csrfAccessDeniedUrl;
    }

    public void setCsrfAccessDeniedUrl(String csrfAccessDeniedUrl) {
        this.csrfAccessDeniedUrl = csrfAccessDeniedUrl;
    }

    public String getSqlHierarchicalRoles() {
        return sqlHierarchicalRoles;
    }

    public void setSqlHierarchicalRoles(String sqlHierarchicalRoles) {
        this.sqlHierarchicalRoles = sqlHierarchicalRoles;
    }

    public String getSqlRolesAndUrl() {
        return sqlRolesAndUrl;
    }

    public void setSqlRolesAndUrl(String sqlRolesAndUrl) {
        this.sqlRolesAndUrl = sqlRolesAndUrl;
    }

    public String getSqlRegexMatchedRequestMapping() {
        return sqlRegexMatchedRequestMapping;
    }

    public void setSqlRegexMatchedRequestMapping(String sqlRegexMatchedRequestMapping) {
        this.sqlRegexMatchedRequestMapping = sqlRegexMatchedRequestMapping;
    }

}
