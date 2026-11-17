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
package org.egovframe.boot.logging.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * 트레이스 기능({@code org.egovframe.logging.trace.enabled=true})을 켜고 {@code logging.pattern.correlation} 을
 * 직접 지정하지 않았으면 traceId/spanId 표시 패턴을 주입한다.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2026.08.24   유지보수  최초 생성
 * </pre>
 * @since 2026.08.24
 */
public class EgovLoggingCorrelationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "egovLoggingCorrelation";
    private static final String CORRELATION_PATTERN_KEY = "logging.pattern.correlation";
    private static final String ENABLED_KEY = "org.egovframe.logging.enabled";
    private static final String TRACE_ENABLED_KEY = "org.egovframe.logging.trace.enabled";
    private static final String TRACE_ID_KEY_PROPERTY = "org.egovframe.logging.trace.trace-id-key";
    private static final String SPAN_ID_KEY_PROPERTY = "org.egovframe.logging.trace.span-id-key";

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.containsProperty(CORRELATION_PATTERN_KEY)) {
            return;
        }
        if (!environment.getProperty(ENABLED_KEY, Boolean.class, true)) {
            return;
        }
        if (!environment.getProperty(TRACE_ENABLED_KEY, Boolean.class, false)) {
            return;
        }

        String traceIdKey = environment.getProperty(TRACE_ID_KEY_PROPERTY, "traceId");
        String spanIdKey = environment.getProperty(SPAN_ID_KEY_PROPERTY, "spanId");
        String pattern = "[%X{" + traceIdKey + ":-}," + "%X{" + spanIdKey + ":-}] ";

        environment.getPropertySources()
                .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, Map.of(CORRELATION_PATTERN_KEY, pattern)));
    }

}
