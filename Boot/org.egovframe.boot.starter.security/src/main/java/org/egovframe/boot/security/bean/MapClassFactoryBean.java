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
package org.egovframe.boot.security.bean;

import lombok.Setter;
import org.egovframe.boot.security.EgovSecurityProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 사용자 정보 mapping 처리 클래스 지정을 처리하는 factory bean 클래스
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
public class MapClassFactoryBean implements FactoryBean<String> {

    private final EgovSecurityProperties properties;
    @Setter
    private String defaultMapClass;

    public MapClassFactoryBean(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getObject() {
        if (ObjectUtils.isEmpty(properties)) {
            throw new NoSuchBeanDefinitionException("### MapClassFactoryBean getJdbcMapClass not found.");
        }

        if (StringUtils.hasText(properties.getJdbcMapClass())) {
            return properties.getJdbcMapClass();
        }

        return defaultMapClass;
    }

    @Override
    public Class<String> getObjectType() {
        return String.class;
    }

}
