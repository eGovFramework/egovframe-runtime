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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MDC 서비스를 위한 Sequence 구현 클래스
 *
 * <p>Desc.: MDC 서비스를 위한 Sequence 구현 클래스</p>
 *
 * <p><b>보안 주의:</b> Reactor {@link Context}의 모든 key-value를 그대로 MDC(로그)에 복사 :
 * 세션ID·토큰·개인정보 노출 우려(CWE-532). 민감 키는 {@link #setExcludedKeys(Set)}로 제외 (기본 빈 집합=제외 없음)</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * </pre>
 * @since 2023.08.31
 */
public class EgovMdcContextConfig {

    public static final String MDC_CONTEXT_KEY = EgovMdcContextConfig.class.getName();

    /** MDC로 복사하지 않을 Reactor Context 키 집합. 기본값은 빈 집합(제외 없음, 기존 동작과 동일). */
    private static volatile Set<String> excludedKeys = Collections.emptySet();

    /**
     * MDC(로그) 복사에서 제외할 민감 Reactor Context 키 지정 (null·빈 집합이면 제외 해제, 기본값).
     *
     * @param keys 로그에서 제외할 Context 키 집합
     */
    public static void setExcludedKeys(Set<String> keys) {
        excludedKeys = (keys == null || keys.isEmpty())
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(keys));
    }

    @PostConstruct
    public void contextOperatorHook() {
        // Run when Publisher Mono/Flux is created
        Hooks.onEachOperator(MDC_CONTEXT_KEY, Operators.lift((scannable, subscriber) -> new EgovSubscriber<>(subscriber)));
    }

    @PreDestroy
    public void cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_KEY);
    }

    /**
     * 스레드의 변경이 있을 때마다 Context의 값을 MDC에 삽입
     */
    public static class EgovSubscriber<T> implements CoreSubscriber<T> {

        private final CoreSubscriber<? super T> coreSubscriber;

        public EgovSubscriber(CoreSubscriber<? super T> coreSubscriber) {
            this.coreSubscriber = coreSubscriber;
        }

        @Override
        public Context currentContext() {
            return coreSubscriber.currentContext();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            coreSubscriber.onSubscribe(subscription);
        }

        @Override
        public void onNext(T t) {
            copyToMdc(coreSubscriber.currentContext());
            coreSubscriber.onNext(t);
        }

        @Override
        public void onError(Throwable throwable) {
            copyToMdc(coreSubscriber.currentContext());
            coreSubscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            copyToMdc(coreSubscriber.currentContext());
            coreSubscriber.onComplete();
        }

        /**
         * Context를 추가하기 위해 Map을 구성하여 MDC에 저장
         */
        private void copyToMdc(Context context) {
            if (context != null && !context.isEmpty()) {
                Set<String> excluded = excludedKeys;
                Map<String, String> map = context.stream()
                        .filter(e -> !excluded.contains(e.getKey().toString()))
                        .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
                MDC.setContextMap(map);
            } else {
                MDC.clear();
            }
        }
    }

}
