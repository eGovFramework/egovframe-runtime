/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.boot.access.bean;

import org.egovframe.boot.access.EgovAccessProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.StringUtils;

/**
 * 사용자 권한 조회 SQL({@code sqlAuthorityUser})을 제공하는 FactoryBean.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * </pre>
 * @since 2025.06.01
 */
public class AuthorityUserFactoryBean implements FactoryBean<String> {

    private final EgovAccessProperties properties;

    public AuthorityUserFactoryBean(EgovAccessProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getObject() {
        if (properties == null) {
            throw new NoSuchBeanDefinitionException("### AuthorityUserFactoryBean EgovAccessProperties is not defined");
        }

        if (StringUtils.hasText(properties.getSqlAuthorityUser())) {
            return properties.getSqlAuthorityUser();
        } else {
            throw new NoSuchBeanDefinitionException("### AuthorityUserFactoryBean getSqlAuthorityUser is empty");
        }
    }

    @Override
    public Class<String> getObjectType() {
        return String.class;
    }

}
