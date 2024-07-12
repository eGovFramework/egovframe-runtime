/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MDC 서비스를 위한 Sequence 구현 클래스
 *
 * <p>Desc.: MDC 서비스를 위한 Sequence 구현 클래스</p>
 *
 * @author ESFC
 * @since 2023.08.31
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   ESFC            최초 생성
 * </pre>
 */
public class EgovMdcContextConfig {

    public static final String MDC_CONTEXT_KEY = EgovMdcContextConfig.class.getName();

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
            coreSubscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            coreSubscriber.onComplete();
        }

        /**
         * Context를 추가하기 위해 Map을 구성하여 MDC에 저장
         */
        private void copyToMdc(Context context) {
            if (context != null && !context.isEmpty()) {
                Map<String, String> map = context.stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
                MDC.setContextMap(map);
            } else {
                MDC.clear();
            }
        }
    }

}
