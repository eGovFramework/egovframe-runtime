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
package org.egovframe.boot.security.secureobject.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 패턴 · HTTP 메서드로 동등성을 비교하는 정규식 요청 매처.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  getPattern() 추가
 * </pre>
 * @since 2025.06.01
 */
public class SelfRegexRequestMatcher implements RequestMatcher {

    private final String pattern;
    private final String httpMethod;
    private final RegexRequestMatcher requestMatcher;

    public SelfRegexRequestMatcher(String pattern, String httpMethod) {
        this(pattern, httpMethod, false);
    }

    public SelfRegexRequestMatcher(String pattern, String httpMethod, boolean caseInsensitive) {
        this.requestMatcher = new RegexRequestMatcher(pattern, httpMethod, caseInsensitive);
        this.pattern = pattern;
        this.httpMethod = httpMethod;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return requestMatcher.matches(request);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SelfRegexRequestMatcher)) {
            return false;
        }

        SelfRegexRequestMatcher key = (SelfRegexRequestMatcher) obj;
        if (!pattern.equals(key.pattern)) {
            return false;
        }

        if (httpMethod == null) {
            return key.httpMethod == null;
        }

        return httpMethod.equals(key.httpMethod);
    }

    @Override
    public int hashCode() {
        int code = 31 ^ pattern.hashCode();

        if (httpMethod != null) {
            code ^= httpMethod.hashCode();
        }

        return code;
    }

}
