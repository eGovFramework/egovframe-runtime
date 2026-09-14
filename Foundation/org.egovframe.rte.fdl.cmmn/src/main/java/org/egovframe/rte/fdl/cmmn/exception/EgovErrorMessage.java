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
package org.egovframe.rte.fdl.cmmn.exception;

import org.springframework.context.MessageSource;

import java.io.Serializable;
import java.util.Locale;

/**
 * 구조화 오류 메시지.
 *
 * <p>오류를 사용자 메시지 한 줄이 아니라 <b>코드 / 사용자 메시지 / 원인(reason) / 조치(solution)</b> 으로 나누어 담는다.
 * 화면은 사용자 메시지를, 로그와 관제는 코드와 원인을, 안내 문구는 조치를 쓰는 식으로 같은 예외에서 각자 필요한
 * 부분을 꺼내 쓸 수 있다.</p>
 *
 * <p>메시지 리소스 키 규약 — 코드가 {@code fail.biz.order} 일 때:</p>
 * <pre>
 * fail.biz.order          = 주문 처리에 실패했습니다.      (사용자 메시지)
 * fail.biz.order.reason   = 재고 수량이 부족합니다.        (원인, 선택)
 * fail.biz.order.solution = 재고 확인 후 다시 시도하세요.   (조치, 선택)
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
public final class EgovErrorMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 원인 메시지 키 접미사 */
    public static final String REASON_SUFFIX = ".reason";

    /** 조치 메시지 키 접미사 */
    public static final String SOLUTION_SUFFIX = ".solution";

    private final String code;
    private final String userMessage;
    private final String reason;
    private final String solution;

    /**
     * @param code        오류 코드(메시지 키)
     * @param userMessage 사용자 메시지
     * @param reason      원인(없으면 null)
     * @param solution    조치(없으면 null)
     */
    public EgovErrorMessage(String code, String userMessage, String reason, String solution) {
        this.code = code;
        this.userMessage = userMessage;
        this.reason = reason;
        this.solution = solution;
    }

    /**
     * 메시지 리소스에서 키 규약({@code code}, {@code code.reason}, {@code code.solution})으로 구조화 메시지를 해석한다.
     * 사용자 메시지가 정의되어 있지 않으면 코드 문자열을, 원인·조치가 없으면 null 을 담는다.
     *
     * @param messageSource 메시지 리소스
     * @param code          오류 코드(메시지 키)
     * @param args          메시지 치환 인자(없으면 null)
     * @param locale        로케일(Locale)
     * @return 구조화 오류 메시지
     */
    public static EgovErrorMessage resolve(MessageSource messageSource, String code, Object[] args, Locale locale) {
        String userMessage = messageSource.getMessage(code, args, code, locale);
        String reason = messageSource.getMessage(code + REASON_SUFFIX, args, null, locale);
        String solution = messageSource.getMessage(code + SOLUTION_SUFFIX, args, null, locale);
        return new EgovErrorMessage(code, userMessage, reason, solution);
    }

    /**
     * @return 오류 코드(메시지 키)
     */
    public String getCode() {
        return code;
    }

    /**
     * @return 사용자 메시지
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * @return 원인. 정의되어 있지 않으면 null
     */
    public String getReason() {
        return reason;
    }

    /**
     * @return 조치. 정의되어 있지 않으면 null
     */
    public String getSolution() {
        return solution;
    }

    /**
     * 로그·관제용 한 줄 표현. 원인과 조치가 있으면 함께 붙인다.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[').append(code).append("] ").append(userMessage);
        if (reason != null) {
            builder.append(" | reason: ").append(reason);
        }
        if (solution != null) {
            builder.append(" | solution: ").append(solution);
        }
        return builder.toString();
    }

}
