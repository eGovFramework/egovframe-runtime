package org.egovframe.boot.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 보안 설정 프로퍼티({@code org.egovframe.security.*}).
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  기본 hash 를 bcrypt 로 변경
 * </pre>
 * @since 2025.06.01
 */
@ConfigurationProperties(prefix = "org.egovframe.security")
@Getter
@Setter
public class EgovSecurityProperties {

    private String loginUrl = "/login";
    private String loginProcessUrl = "/loginProcess";
    private String loginFailureUrl = "/loginFailure";
    private String logoutUrl = "/logout";
    private String logoutSuccessUrl = "/";
    private String accessDeniedUrl = "/accessDenied";
    private String dataSource = "dataSource";
    private String jdbcUsersByUsernameQuery = "SELECT USER_ID,PASSWORD,ENABLED,USER_NAME,BIRTH_DAY,SSN FROM USERS WHERE USER_ID = ?";
    private String jdbcAuthoritiesByUsernameQuery = "SELECT USER_ID,AUTHORITY FROM AUTHORITIES WHERE USER_ID = ?";
    private String jdbcMapClass = "org.egovframe.boot.security.userdetails.DefaultMapUserDetailsMapping";
    private String requestMatcherType = "regex";
    private String hash = "bcrypt";
    private boolean hashBase64 = true;
    private int concurrentMaxSessons = 1;
    private String concurrentExpiredUrl = "/login";
    private boolean errorIfMaximumExceeded = false;
    private String defaultTargetUrl = "/";
    private boolean alwaysUseDefaultTargetUrl = false;
    private boolean sniff = true;
    private String xframeOptions = "SAMEORIGIN";
    private boolean xssProtection = true;
    private boolean cacheControl = false;
    private boolean csrf = false;
    private String csrfAccessDeniedUrl = "/csrfAccessDenied";
    private String permitAllList = "/css/**, /js/**, /images/**, /resource/**, /public/**";
    private String sqlHierarchicalRoles;
    private String sqlRolesAndUrl;
    private String sqlRolesAndMethod;
    private String sqlRolesAndPointcut;
    private String sqlRegexMatchedRequestMapping;
    private boolean supportMethod;
    private boolean supportPointcut;

    @JsonIgnore
    public List<String> getPermitAllPatterns() {
        if (permitAllList == null || permitAllList.isBlank()) return List.of();
        return Arrays.stream(permitAllList.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

}
