package org.egovframe.boot.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="org.egovframe.security")
@Data
public class EgovSecurityProperties {
    private String loginUrl = "/index.html";
    private String logoutUrl;
    private String loginFailureUrl;
    private String logoutSuccessUrl;
    private String accessDeniedUrl = "/index.html";
    private String dataSource;
    private String jdbcUsersByUsernameQuery = "SELECT USER_ID, PASSWORD AS PASSWORD FROM USERS WHERE USER_ID = ?";
    private String jdbcAuthoritiesByUsernameQuery = "SELECT USER_ID, AUTHOR_CODE AUTHORITY FROM AUTHORITIES WHERE USER_ID = ?";
    private String jdbcMapClass = "org.egovframe.boot.security.userdetails.DefaultMapUserDetailsMapping";
    private String requestMatcherType;
    private String hash;
    private boolean hashBase64 = true;
    private int concurrentMaxSessons = -1;
    private String concurrentExpiredUrl = "/index.html";
    private boolean errorIfMaximumExceeded = false;
    private String defaultTargetUrl;
    private boolean alwaysUseDefaultTargetUrl;
    private boolean sniff;
    private String xframeOptions;
    private boolean xssProtection;
    private boolean cacheControl;
    private boolean csrf;
    private boolean supportMethod;
    private boolean supportPointcut;
    private String csrfAccessDeniedUrl = "/index.html";;
    private String sqlRolesAndUrl;
    private String sqlRolesAndMethod;
    private String sqlRolesAndPointcut;
    private String sqlRegexMatchedRequestMapping;
    private String sqlHierarchicalRoles;
    private String roleHierarchyString;
    private String excludeList;
}
