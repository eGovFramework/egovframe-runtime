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

import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * 리액티브 요청의 사용자 ID 를 Reactor Context 와 교환 속성에 싣는 필터.
 *
 * <p>Spring Security 리액티브 {@code WebFilterChainProxy}(순서 -100) <b>뒤</b>(기본 순서 {@value #DEFAULT_ORDER})에서
 * {@link ServerWebExchange#getPrincipal()} 의 이름을 읽어 ① 교환 속성 {@link EgovReactiveWebAccessLogFilter#ATTR_USER_ID} 와
 * ② 하류 Reactor Context 키 {@value #USER_ID_KEY} 에 넣는다. Principal 이 없거나 이름이 비어 있으면 {@value #ANONYMOUS_USER} 를 넣는다.</p>
 *
 * <ul>
 *   <li>① 로 {@link EgovReactiveWebAccessLogFilter} 의 {@code userId=} 가 채워진다(접근로그 필터는 {@code doFinally} 에서 속성을 읽는다)</li>
 *   <li>② 는 {@link EgovMdcContextConfig}(Context → MDC 복사 훅)가 설치돼 있으면 로그 MDC {@code userId} 로도 복사되어, 로그 패턴
 *       {@code %X{userId}} 와 {@code ReactiveAuditorAware} 구현이 같은 값을 본다 — 서블릿 스택의 MDC {@code userId} 관례와 같은 키다</li>
 *   <li>Spring Security 에 대한 코드 의존은 없다 — 사용자 원천은 {@code Principal} 이며, 인증 프레임워크가 무엇이든 교환에 Principal 을 실어 두면 동작한다</li>
 * </ul>
 *
 * <p>빈으로 등록해야 동작한다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.30	실행환경 개발팀		최초 생성
 * </pre>
 */
public class EgovReactiveUserIdWebFilter implements WebFilter, Ordered {

    /** Reactor Context 의 사용자 ID 키(서블릿 스택 MDC 키와 같은 관례) */
    public static final String USER_ID_KEY = "userId";

    /** Principal 이 없는 요청의 사용자 ID 대체값 */
    public static final String ANONYMOUS_USER = "anonymous";

    /** 기본 순서 — Spring Security 리액티브 필터 체인(-100) 뒤, 앱 필터 앞 */
    public static final int DEFAULT_ORDER = 0;

    private int order = DEFAULT_ORDER;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.<Principal>getPrincipal()
                .map(Principal::getName)
                .filter(name -> name != null && !name.isBlank())
                .defaultIfEmpty(ANONYMOUS_USER)
                .flatMap(userId -> {
                    exchange.getAttributes().put(EgovReactiveWebAccessLogFilter.ATTR_USER_ID, userId);
                    return chain.filter(exchange)
                            .contextWrite(context -> context.put(USER_ID_KEY, userId));
                });
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * 필터 순서를 설정한다(기본 {@value #DEFAULT_ORDER} — 인증 필터 체인 뒤에 두어야 Principal 이 보인다).
     */
    public void setOrder(int order) {
        this.order = order;
    }
}
