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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * egov-security http create
 *
 * <p>Desc.: egov-security http bean의 xml string 를 생성 Bean</p>
 *
 * @author 장동한
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.03.12	장동한			SpringSecurity 4.x 업그레이드 추가
 * 2020.05.27	ESFC			CSRF Access Denied handler 설정 추가
 * 2023.08.31	ESFC			Spring 표현 언어(SpEL) 설정 옵션 추가
 * 2026.10.25	실행환경 개발팀		보안 헤더 개별 제어(sniff=false 가 기본 헤더 전체를 끄던 문제 수정),
 *                          X-Frame-Options 값 검증, cacheControl=true 경고, 로그인 실패 메시지 일반화
 * </pre>
 * @since 2016.07.03
 */
public class EgovSecurityHttp {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSecurityHttp.class);

    private static EgovSecurityHttp instance;

    public EgovSecurityHttp() {
    }

    public synchronized static EgovSecurityHttp getInstance() {
        if (instance == null) {
            instance = new EgovSecurityHttp();
        }
        return instance;
    }

    /**
     * egov-security http bean의 xml string 를 생성
     *
     * @param useExpressions : Spring 표현 언어(SpEL) 설정 옵션(활성:true, 비활성:false)
     * @param Sniff          : 응답에 대한 브라우저의 MIME 가로채기를 방지 옵션(활성:true, 비활성:false)
     * @param XFrameOptions  : 프레임셋 동작여부 옵션(DENY:거부, SAMEORIGIN:허용)
     * @param XssProtection  : 브라우저가 XSS 공격에 사용될 수 있는 스크립트를 실행하지 않음 옵션(활성:true, 비활성:false)
     * @param CacheControl   : 브라우저 캐시를 수동으로 제어하기 위한 설정(캐시비활성:true, 캐시활성:false)
     * @param Csrf           : HTTP 요청과 악의적 인 웹 사이트의 요청을 거부 옵션(활성:true, 비활성:false)
     * @return InputStreamResource
     */
    public InputStreamResource getHttp(
            boolean useExpressions,
            boolean Sniff,
            String XFrameOptions,
            boolean XssProtection,
            boolean CacheControl,
            boolean Csrf
    ) {

        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<beans:beans xmlns=\"http://www.springframework.org/schema/security\"\n");
        sb.append("    xmlns:beans=\"http://www.springframework.org/schema/beans\"\n");
        sb.append("    xmlns:aop=\"http://www.springframework.org/schema/aop\"\n");
        sb.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("    xmlns:context=\"http://www.springframework.org/schema/context\"\n");
        sb.append("    xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.3.xsd\n");
        sb.append("        http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.3.xsd\n");
        sb.append("        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.3.xsd\n");
        sb.append("        http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security.xsd\">\n");
        sb.append("    <http entry-point-ref=\"loginUrlAuthenticationEntryPoint\"\n");
        sb.append("        request-matcher-ref=\"requestMatcherTypeFactoryBean\"\n");
        sb.append("        disable-url-rewriting=\"false\"\n");
        sb.append("        use-expressions=\"").append(useExpressions).append("\">\n");

        // 기본 보안 헤더(HSTS·Cache-Control·X-Content-Type-Options 등)는 항상 켜 두고, 옵션별로 개별 제어한다.
        // (종전에는 sniff=false 이면 defaults-disabled="true" 가 되어 모든 기본 보안 헤더가 함께 꺼졌다)
        sb.append("        <headers defaults-disabled=\"false\">\n");

        // X-Content-Type-Options
        sb.append("            <content-type-options disabled=\"").append(!Sniff).append("\"/>\n");

        // X-Frame-Options (설정 값을 XML 에 그대로 이어 붙이지 않도록 허용 값만 사용)
        String frameOptions = (XFrameOptions == null) ? "" : XFrameOptions.trim().toUpperCase(Locale.ROOT);
        if ("DISABLE".equals(frameOptions)) {
            sb.append("            <frame-options disabled=\"true\"/>\n");
        } else if ("SAMEORIGIN".equals(frameOptions)) {
            sb.append("            <frame-options policy=\"SAMEORIGIN\"/>\n");
        } else {
            if (!frameOptions.isEmpty() && !"DENY".equals(frameOptions)) {
                LOGGER.warn("Unsupported xframeOptions '{}' - DENY is applied.", XFrameOptions);
            }
            sb.append("            <frame-options policy=\"DENY\"/>\n");
        }

        if (XssProtection) {
            sb.append("            <xss-protection enabled=\"true\" block=\"true\"/>\n");
        }

        // Cache-Control (주의: cacheControl=true 는 보호 헤더를 "비활성화"한다 - 이름과 반대 동작, CWE-525)
        if (CacheControl) {
            LOGGER.warn("cacheControl=true - this DISABLES the protective Cache-Control/Pragma/Expires " +
                    "response headers (the property name is misleading). Sensitive pages may be cached by " +
                    "browsers/proxies. Leave cacheControl false (default) to keep these headers enabled.");
        }
        sb.append("            <cache-control disabled=\"").append(CacheControl).append("\"/>\n");

        sb.append("        </headers>\n");

        // Csrf 설정 활성화
        if (Csrf) {
            sb.append("        <csrf disabled=\"false\"/>\n");
        }
        // Csrf 설정 비활성화
        else {
            sb.append("        <csrf disabled=\"true\"/>\n");
        }

        // 로그인 실패 원인(계정 없음·잠김 등)을 화면에 구분해 노출하지 않도록 일반화 처리기를 사용한다(CWE-203).
        sb.append("        <form-login login-page=\"/egov_security_login\" username-parameter=\"egov_security_username\" password-parameter=\"egov_security_password\" authentication-failure-handler-ref=\"egovLoginFailHandler\"/>\n");
        sb.append("        <logout logout-url=\"/egov_security_logout\"/>\n");
        sb.append("        <anonymous/>\n");
        sb.append("        <session-management session-authentication-strategy-ref=\"sas\"/>\n");
        sb.append("        <custom-filter before=\"FILTER_SECURITY_INTERCEPTOR\" ref=\"filterSecurityInterceptor\"/>\n");
        sb.append("        <custom-filter position=\"CONCURRENT_SESSION_FILTER\" ref=\"concurrencyFilter\"/>\n");
        sb.append("        <access-denied-handler ref=\"csrfAccessDeniedHandler\"/>\n");
        sb.append("    </http>\n");
        sb.append("    <beans:bean id=\"egovLoginFailHandler\" class=\"org.egovframe.rte.fdl.security.intercept.EgovLoginFailHandler\">\n");
        sb.append("        <beans:constructor-arg value=\"/egov_security_login?error\"/>\n");
        sb.append("    </beans:bean>\n");
        sb.append("</beans:beans>\n");

        LOGGER.debug("EgovSecurityHttp START ===");
        LOGGER.debug("EgovSecurityHttp create http string");
        LOGGER.debug(sb.toString());
        LOGGER.debug("EgovSecurityHttp END ===");

        return new InputStreamResource(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

}
