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
package org.egovframe.boot.logging.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 요청 처리 구간 동안 traceId/spanId 를 MDC 에 설정한다.
 * Micrometer {@link Tracer} 빈이 있으면 그 값을, 없으면 요청 단위로 생성한 대체 ID 를 사용한다.
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
public class EgovTraceLoggingFilter extends OncePerRequestFilter {

    private final ObjectProvider<Tracer> tracerProvider;
    private final String traceIdKey;
    private final String spanIdKey;

    public EgovTraceLoggingFilter(ObjectProvider<Tracer> tracerProvider, String traceIdKey, String spanIdKey) {
        this.tracerProvider = tracerProvider;
        this.traceIdKey = traceIdKey;
        this.spanIdKey = spanIdKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = null;
        String spanId = null;

        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer != null) {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                TraceContext context = currentSpan.context();
                traceId = context.traceId();
                spanId = context.spanId();
            }
        }

        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put(traceIdKey, traceId);
        if (spanId != null && !spanId.isBlank()) {
            MDC.put(spanIdKey, spanId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(traceIdKey);
            MDC.remove(spanIdKey);
        }
    }

}
