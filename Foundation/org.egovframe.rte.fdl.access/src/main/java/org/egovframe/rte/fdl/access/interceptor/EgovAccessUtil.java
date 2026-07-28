/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.access.interceptor;

import org.springframework.util.AntPathMatcher;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ant / Regular Expression Style Path Check
 *
 * <p>Desc.: Ant / Regular Expression Style Path Check</p>
 *
 * @author 유지보수
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2019.10.01	유지보수            최초 생성
 * 2026.07.27	z3rotig4r          접근 검사 핫패스 최적화 — 정규식 Pattern 캐싱, AntPathMatcher 재사용
 * </pre>
 * @since 2019.10.01
 */
public class EgovAccessUtil {

    // AntPathMatcher는 match() 호출이 thread-safe하며 내부 토큰화 캐시를 재사용하도록 설계되어 있어 공유 인스턴스로 둔다.
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    // 정규식 컴파일 결과(NFA)를 패턴 문자열로 캐싱해 매 요청마다의 재컴파일을 제거한다.
    // 키는 관리자 권한 설정에서 오는 유한한 authUrl 패턴이라(요청 입력이 아님) 캐시 크기는 사실상 유계다.
    private static final ConcurrentMap<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    public static boolean antMatcher(String pattern, String inputString) {
        return ANT_PATH_MATCHER.match(pattern, inputString);
    }

    public static boolean regexMatcher(String pattern, String inputString) {
        Matcher m = PATTERN_CACHE.computeIfAbsent(pattern, Pattern::compile).matcher(inputString);
        return m.matches();
    }

}
