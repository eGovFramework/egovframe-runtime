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
package org.egovframe.rte.fdl.access.config;

import org.egovframe.rte.fdl.access.interceptor.EgovAccessInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * RequestMappingHandlerMapping 빈에 EgovAccessInterceptor(MappedInterceptor)를 선두에 추가하는 BeanPostProcessor.
 *
 * @author 유지보수
 * @version 6.0
 * @since 2025.06.01
 */
public class EgovAccessHandlerMappingPostProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovAccessHandlerMappingPostProcessor.class);
    private static final String ADAPTED_INTERCEPTORS_FIELD = "adaptedInterceptors";

    private final EgovAccessConfig accessConfig;

    public EgovAccessHandlerMappingPostProcessor(EgovAccessConfig accessConfig) {
        this.accessConfig = accessConfig;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof RequestMappingHandlerMapping)) {
            return bean;
        }

        if (!"session".equals(accessConfig.getGlobalAuthen())) {
            return bean;
        }

        String profile = System.getProperty("spring.profiles.active");
        boolean shouldRegister = ObjectUtils.isEmpty(profile) || (profile != null && profile.contains(accessConfig.getGlobalAuthen()));
        if (!shouldRegister) {
            return bean;
        }

        if (accessConfig.getLoginUrl() != null) {
            EgovAccessConfigShare.DEF_LOGIN_URL = accessConfig.getLoginUrl();
        }
        if (accessConfig.getAccessDeniedUrl() != null) {
            EgovAccessConfigShare.DEF_ACCESS_DENIED_URL = accessConfig.getAccessDeniedUrl();
        }
        if (accessConfig.getRequestMatcherType() != null) {
            EgovAccessConfigShare.DEF_REQUEST_MATCH_TYPE = accessConfig.getRequestMatcherType();
        }

        AbstractHandlerMapping handlerMapping = (AbstractHandlerMapping) bean;

        // addPathPatterns / excludePathPatterns와 동일하게 적용 (기동 시 /, /index.do 등에서 리다이렉트 방지)
        String mappingPath = StringUtils.hasText(accessConfig.getMappingPath()) ? accessConfig.getMappingPath() : "/**/*.do";
        String[] excludePatterns = Stream.of(accessConfig.getExcludeList().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);

        HandlerInterceptor toAdd = new MappedInterceptor(
                new String[]{mappingPath},
                excludePatterns,
                new EgovAccessInterceptor(accessConfig)
        );

        try {
            Field field = AbstractHandlerMapping.class.getDeclaredField(ADAPTED_INTERCEPTORS_FIELD);
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<HandlerInterceptor> list = (List<HandlerInterceptor>) field.get(bean);
            if (list != null) {
                list.add(0, toAdd);
                LOGGER.debug("EgovAccessInterceptor (MappedInterceptor) added to existing RequestMappingHandlerMapping (beanName={}, include={}, exclude={})",
                        beanName, mappingPath, Arrays.toString(excludePatterns));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.debug("Could not add EgovAccessInterceptor to RequestMappingHandlerMapping: {}", e.getMessage());
        }

        return bean;
    }

}
