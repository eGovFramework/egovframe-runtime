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
package org.egovframe.rte.psl.reactive.r2dbc.audit;

import org.springframework.data.domain.ReactiveAuditorAware;
import reactor.core.publisher.Mono;

/**
 * Reactor Context 의 사용자 ID 를 감사자로 돌려주는 {@link ReactiveAuditorAware}.
 *
 * <p>현재 구독의 Reactor Context 키 {@value #USER_ID_KEY} 를 읽는다. 값은 인증 주체({@code Principal})의 이름을 Context 에 싣는
 * 웹 필터(Security 체인 뒤)가 넣는다. 서블릿 스택의 로그 관례인 MDC {@code userId} 와 같은 키이며, {@code fdl.reactive} 의
 * {@code EgovMdcContextConfig} 훅을 설치하면 같은 값이 MDC 로도 복사된다. 웹 모듈에 대한 코드 의존은 없다 — {@code psl} 모듈이
 * {@code fdl.reactive} 를 끌면 WebFlux 가 따라오기 때문에 키 문자열만 공유한다.</p>
 *
 * <p>키가 없으면 <b>빈 {@code Mono}</b> 를 돌려준다 — 여기서 임의 대체값을 만들지 않는다(익명 요청의 대체값은 필터가 정하고, 필터 없는 배치·테스트
 * 구독에서는 등록자·수정자가 비어 있는 것이 정직하다). 그 경우 감사 일시만 기록된다.</p>
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
public class EgovContextReactiveAuditorAware implements ReactiveAuditorAware<String> {

    /** Reactor Context 의 사용자 ID 키(서블릿 MDC 키와 같은 관례) */
    public static final String USER_ID_KEY = "userId";

    @Override
    public Mono<String> getCurrentAuditor() {
        return Mono.deferContextual(context -> Mono.justOrEmpty(context.getOrEmpty(USER_ID_KEY)))
                .map(Object::toString)
                .filter(userId -> !userId.isBlank());
    }
}
