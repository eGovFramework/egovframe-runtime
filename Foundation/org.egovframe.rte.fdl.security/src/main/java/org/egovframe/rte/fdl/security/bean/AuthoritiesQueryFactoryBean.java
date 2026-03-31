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
package org.egovframe.rte.fdl.security.bean;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 권한관리(authority) 정보 취득을 위한 query 지정을 처리하는 factory bean 클래스
 *
 * <p>Desc.: 설정 간소화 처리에 사용되는 내부 factory bean</p>
 *
 * @author Vincent Han
 * @version 3.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.03.12	한성곤				Spring Security 설정 간소화 기능 추가
 * 2025.06.01	유지보수				Spring Security 6.x 대응 (생성자 주입 방식으로 변경)
 * </pre>
 * @since 2025.06.01
 */
public class AuthoritiesQueryFactoryBean implements FactoryBean<String> {

    private static final String AUTHOR_DEFAULT_QUERY = "select user_id, authority from authorites where user_id = ?";
    private final EgovSecurityConfig config;

    public AuthoritiesQueryFactoryBean(EgovSecurityConfig config) {
        this.config = config;
    }

    @Override
    public String getObject() {
        if (ObjectUtils.isEmpty(config)) {
            throw new NoSuchBeanDefinitionException("### AuthoritiesQueryFactoryBean getJdbcAuthoritiesByUsernameQuery not found.");
        }

        if (StringUtils.hasText(config.getJdbcAuthoritiesByUsernameQuery())) {
            return config.getJdbcAuthoritiesByUsernameQuery();
        }

        return AUTHOR_DEFAULT_QUERY;
    }

    @Override
    public Class<String> getObjectType() {
        return String.class;
    }

}
