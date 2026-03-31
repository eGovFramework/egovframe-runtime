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
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ObjectUtils;

public class RequestMatcherTypeFactoryBean implements FactoryBean<RequestMatcher> {

    private final EgovSecurityConfig config;

    public RequestMatcherTypeFactoryBean(EgovSecurityConfig config) {
        this.config = config;
    }

    public RequestMatcher getObject() throws Exception {
        if (ObjectUtils.isEmpty(config)) {
            throw new NoSuchBeanDefinitionException("### RequestMatcherTypeFactoryBean EgovSecurityProperties not defined");
        }

        return AnyRequestMatcher.INSTANCE;
    }

    public Class<RequestMatcher> getObjectType() {
        return RequestMatcher.class;
    }

}
