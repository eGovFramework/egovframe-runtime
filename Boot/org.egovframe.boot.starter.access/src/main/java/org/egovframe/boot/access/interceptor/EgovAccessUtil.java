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
package org.egovframe.boot.access.interceptor;

import org.springframework.util.AntPathMatcher;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ant / 정규식 URL 패턴 매칭 유틸리티.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  정규식 전체 일치(matches) 적용, 패턴 캐시
 * </pre>
 * @since 2025.06.01
 */
public class EgovAccessUtil {

    // thread-safe 하므로 공유
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    // 정규식 컴파일 결과 캐시(키는 설정된 권한 URL 패턴이므로 크기가 제한됨)
    private static final ConcurrentMap<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    public static boolean antMatcher(String pattern, String inputString) {
        return ANT_PATH_MATCHER.match(pattern, inputString);
    }

    /** 요청 경로 전체가 패턴과 일치해야 true (부분 일치 허용 안 함) */
    public static boolean regexMatcher(String pattern, String inputString) {
        Matcher m = PATTERN_CACHE.computeIfAbsent(pattern, Pattern::compile).matcher(inputString);
        return m.matches();
    }

}
