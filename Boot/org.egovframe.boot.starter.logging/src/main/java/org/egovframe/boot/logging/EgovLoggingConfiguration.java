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
package org.egovframe.boot.logging;

import io.micrometer.tracing.Tracer;
import org.egovframe.boot.logging.filter.EgovRequestLoggingFilter;
import org.egovframe.boot.logging.filter.EgovTraceLoggingFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 요청 로깅 · 트레이스 로깅 필터 자동 구성.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2026.08.24   유지보수  최초 생성
 * 2026.10.23   유지보수  @AutoConfiguration 전환
 * </pre>
 * @since 2026.08.24
 */
@AutoConfiguration
@EnableConfigurationProperties(EgovLoggingProperties.class)
@ConditionalOnProperty(prefix = "org.egovframe.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EgovLoggingConfiguration {

    private final EgovLoggingProperties properties;

    public EgovLoggingConfiguration(EgovLoggingProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "org.egovframe.logging.trace", name = "enabled", havingValue = "true", matchIfMissing = false)
    public FilterRegistrationBean<EgovTraceLoggingFilter> egovTraceLoggingFilter(ObjectProvider<Tracer> tracerProvider) {
        EgovLoggingProperties.Trace trace = properties.getTrace();
        EgovTraceLoggingFilter filter = new EgovTraceLoggingFilter(tracerProvider, trace.getTraceIdKey(), trace.getSpanIdKey());

        FilterRegistrationBean<EgovTraceLoggingFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    @Bean
    @ConditionalOnProperty(prefix = "org.egovframe.logging.request", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<EgovRequestLoggingFilter> egovRequestLoggingFilter() {
        EgovRequestLoggingFilter filter = new EgovRequestLoggingFilter(properties.getRequest().getExcludePatterns());

        FilterRegistrationBean<EgovRequestLoggingFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registrationBean;
    }

}
