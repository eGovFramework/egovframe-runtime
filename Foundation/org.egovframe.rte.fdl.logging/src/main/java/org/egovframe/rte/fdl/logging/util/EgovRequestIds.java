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
package org.egovframe.rte.fdl.logging.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 게이트웨이·프록시가 넘긴 요청 식별자({@code X-Request-Id} 등)의 형식을 검증하는 유틸리티 클래스.
 * 허용 형식(영숫자와 {@code . _ : -} 1~128자)이 아니면 새 UUID를 생성한다(로그 인젝션 CWE-117 방지).
 *
 * <pre>
 * String requestId = EgovRequestIds.acceptOrGenerate(request.getHeader("X-Request-Id"));
 * MDC.put("requestId", requestId);
 * response.setHeader("X-Request-Id", requestId);
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public final class EgovRequestIds {

    /** 허용 형식: 영숫자·점·밑줄·콜론·하이픈, 1~128자. */
    public static final Pattern ALLOWED = Pattern.compile("[A-Za-z0-9._:\\-]{1,128}");

    private EgovRequestIds() {
    }

    /**
     * 허용 형식이면 그대로, 아니면 새 UUID를 반환한다.
     *
     * @param incoming 요청 헤더 값({@code null} 허용)
     * @return 로그·헤더에 실어도 안전한 요청 식별자
     */
    public static String acceptOrGenerate(String incoming) {
        return isAcceptable(incoming) ? incoming : UUID.randomUUID().toString();
    }

    /**
     * 허용 형식 여부를 반환한다.
     *
     * @param incoming 요청 헤더 값({@code null} 허용)
     * @return 영숫자와 {@code . _ : -} 만으로 된 1~128자이면 {@code true}
     */
    public static boolean isAcceptable(String incoming) {
        return incoming != null && ALLOWED.matcher(incoming).matches();
    }

}
