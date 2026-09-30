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
package org.egovframe.rte.psl.data.jpa.audit;

import org.slf4j.MDC;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * SLF4J MDC 의 사용자 ID 를 감사자로 돌려주는 {@link AuditorAware}.
 *
 * <p>MDC 키 {@value #MDC_USER_ID} 를 읽는다. 서블릿 스택의 로그 관례(로그 패턴 {@code %X{userId}})와 같은 키이며, 값은 애플리케이션의
 * 필터나 인증 성공 지점이 인증 주체 이름으로 넣는다. 관측성·접속기록 모듈에 대한 코드 의존은 없다 — MDC 는 SLF4J 자체 기능이다.
 * 인증 프레임워크(Spring Security)에도 의존하지 않는다.</p>
 *
 * <p>값이 없거나 비어 있으면 {@link Optional#empty()} 를 돌려준다 — 여기서 임의 대체값을 만들지 않는다(익명 요청의 대체값은 필터가 정하고,
 * 필터 없는 배치·비동기 스레드에서는 등록자·수정자가 비어 있는 것이 정직하다). 그 경우 감사 일시만 기록된다. MDC 는 스레드 로컬이므로
 * 서블릿 비동기·배치처럼 요청 스레드가 아닌 곳에서는 비어 있을 수 있다.</p>
 *
 * <p>활성화: 구성 클래스에 {@code @EnableJpaAuditing} + 이 클래스를 {@code AuditorAware} 빈으로 등록한다.</p>
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
public class EgovMdcAuditorAware implements AuditorAware<String> {

    /** 접속자 MDC 키(서블릿 스택 로그 관례 {@code userId}) */
    public static final String MDC_USER_ID = "userId";

    @Override
    public Optional<String> getCurrentAuditor() {
        String userId = MDC.get(MDC_USER_ID);
        return (userId == null || userId.isBlank()) ? Optional.empty() : Optional.of(userId);
    }
}
