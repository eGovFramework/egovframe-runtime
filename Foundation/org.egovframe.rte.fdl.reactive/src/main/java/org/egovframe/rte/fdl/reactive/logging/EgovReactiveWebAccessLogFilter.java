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
package org.egovframe.rte.fdl.reactive.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * WebFlux 요청 단위 접근로그 필터 — 요청 1건마다 전용 로거 {@value #LOGGER_NAME} 에 1줄을 남긴다.
 *
 * <pre>method=GET uri=/api/list status=200 elapsedMs=12 requestId=… clientIp=… userId=- [error=SimpleName]</pre>
 *
 * <ul>
 *   <li>{@code doFinally} 에서 기록하므로 오류·취소로 끝난 요청도 남는다. 오류는 status 500 과 {@code error=} 로 표시하고
 *       정상 INFO, 오류·5xx 는 WARN 으로 남긴다</li>
 *   <li>requestId·clientIp·userId 는 exchange 속성({@link #ATTR_REQUEST_ID}·{@link #ATTR_CLIENT_IP}·{@link #ATTR_USER_ID})이
 *       있으면 그 값을 쓰고, 없으면 각각 {@code ServerHttpRequest.getId()}·원격 주소·{@code -} 를 쓴다. 게이트웨이 식별자나
 *       프록시 헤더를 해석하는 필터가 있으면 이 필터보다 안쪽(더 큰 order)에 두고 속성을 채우면 된다</li>
 *   <li>쿼리스트링은 기본으로 기록하지 않고(민감 파라미터 유출 방지), 제외 패턴은 Ant 형식(애플리케이션 내 경로 기준)</li>
 *   <li>기록 실패는 요청 처리에 전파되지 않는다</li>
 * </ul>
 *
 * <p>빈으로 등록해야 동작한다. 전용 로거를 별도 파일로 나누려면 로깅 설정에 {@value #LOGGER_NAME} 이름의 Logger 를 추가한다.</p>
 *
 * <pre>
 * &#64;Bean
 * public EgovReactiveWebAccessLogFilter webAccessLogFilter() {
 *     EgovReactiveWebAccessLogFilter filter = new EgovReactiveWebAccessLogFilter();
 *     filter.setExcludePathPatterns(List.of("/css/**", "/actuator/**"));
 *     return filter;
 * }
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public class EgovReactiveWebAccessLogFilter implements WebFilter, Ordered {

    /** 전용 로거(채널) 이름. */
    public static final String LOGGER_NAME = "EGOV_WEB_ACCESS";

    /** exchange 속성 — 요청 식별자(없으면 {@code ServerHttpRequest.getId()}). */
    public static final String ATTR_REQUEST_ID = EgovReactiveWebAccessLogFilter.class.getName() + ".requestId";

    /** exchange 속성 — 클라이언트 IP(없으면 원격 주소). */
    public static final String ATTR_CLIENT_IP = EgovReactiveWebAccessLogFilter.class.getName() + ".clientIp";

    /** exchange 속성 — 사용자 식별자(없으면 {@code -}). */
    public static final String ATTR_USER_ID = EgovReactiveWebAccessLogFilter.class.getName() + ".userId";

    /** 기본 순서 — 가장 바깥쪽에 가깝게 두어 안쪽 필터에서 끝난 요청도 기록한다. */
    public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    /** 기본 제외 패턴 — 정적 자원·모니터링 경로. */
    public static final List<String> DEFAULT_EXCLUDE_PATTERNS = List.of(
            "/css/**", "/js/**", "/images/**", "/webjars/**", "/actuator/**", "/favicon.ico");

    private static final Logger ACCESS = LoggerFactory.getLogger(LOGGER_NAME);
    private static final Logger LOGGER = LoggerFactory.getLogger(EgovReactiveWebAccessLogFilter.class);
    private static final String NONE = "-";

    private final PathMatcher pathMatcher = new AntPathMatcher();
    private List<String> excludePathPatterns = DEFAULT_EXCLUDE_PATTERNS;
    private boolean includeQueryString;
    private int order = DEFAULT_ORDER;

    /**
     * 제외 경로 패턴(Ant 형식, 애플리케이션 내 경로 기준). null 이면 제외 없음.
     *
     * @param patterns 제외 패턴 목록
     */
    public void setExcludePathPatterns(List<String> patterns) {
        this.excludePathPatterns = (patterns == null) ? Collections.emptyList() : List.copyOf(patterns);
    }

    /**
     * 쿼리스트링을 경로에 덧붙여 기록할지 여부(기본 false).
     *
     * @param includeQueryString true 면 {@code uri=/path?query} 로 기록
     */
    public void setIncludeQueryString(boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (isExcluded(exchange)) {
            return chain.filter(exchange);
        }
        long started = System.nanoTime();
        AtomicReference<Throwable> error = new AtomicReference<>();
        return chain.filter(exchange)
                .doOnError(error::set)
                .doFinally(signal -> record(exchange, started, error.get()));
    }

    private boolean isExcluded(ServerWebExchange exchange) {
        if (excludePathPatterns.isEmpty()) {
            return false;
        }
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        for (String pattern : excludePathPatterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private void record(ServerWebExchange exchange, long started, Throwable error) {
        try {
            long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
            HttpStatusCode code = exchange.getResponse().getStatusCode();
            String status = (code != null) ? String.valueOf(code.value()) : (error != null ? "500" : NONE);
            String uri = exchange.getRequest().getPath().value();
            String query = exchange.getRequest().getURI().getRawQuery();
            if (includeQueryString && query != null && !query.isEmpty()) {
                uri = uri + "?" + query;
            }
            StringBuilder line = new StringBuilder(160)
                    .append("method=").append(exchange.getRequest().getMethod())
                    .append(" uri=").append(uri)
                    .append(" status=").append(status)
                    .append(" elapsedMs=").append(elapsedMs)
                    .append(" requestId=").append(attribute(exchange, ATTR_REQUEST_ID, exchange.getRequest().getId()))
                    .append(" clientIp=").append(attribute(exchange, ATTR_CLIENT_IP, remoteHost(exchange)))
                    .append(" userId=").append(attribute(exchange, ATTR_USER_ID, NONE));
            if (error != null) {
                line.append(" error=").append(error.getClass().getSimpleName());
            }
            boolean failed = error != null || (code != null && code.is5xxServerError());
            emit(failed, line.toString());
        } catch (RuntimeException e) {
            // 접근 기록 실패가 요청 처리에 새지 않도록 격리한다
            LOGGER.debug("Failed to write web access log", e);
        }
    }

    /**
     * 발신 지점 — 기본은 전용 로거(정상 INFO, 오류·5xx WARN). 다른 파이프라인으로 보내려면 오버라이드한다.
     *
     * @param failed 오류 또는 5xx 로 끝났으면 true
     * @param line   접근로그 1줄
     */
    protected void emit(boolean failed, String line) {
        if (failed) {
            ACCESS.warn(line);
        } else {
            ACCESS.info(line);
        }
    }

    private static String attribute(ServerWebExchange exchange, String name, String fallback) {
        Object value = exchange.getAttributes().get(name);
        if (value == null) {
            return (fallback == null || fallback.isEmpty()) ? NONE : fallback;
        }
        return Objects.toString(value);
    }

    private static String remoteHost(ServerWebExchange exchange) {
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        if (remote == null) {
            return NONE;
        }
        InetAddress address = remote.getAddress();
        return (address != null) ? address.getHostAddress() : remote.getHostString();
    }

}
