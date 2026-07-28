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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * {@link EgovAccessUtil}의 경로 매칭이 Pattern 캐싱·AntPathMatcher 재사용 후에도
 * 기존과 동일한 매칭 결과를 유지하는지, 그리고 동일 패턴이 재컴파일 없이 캐시되는지 검증한다.
 */
class EgovAccessUtilTest {

    @Test
    void antMatcher_behaviorUnchanged() {
        assertTrue(EgovAccessUtil.antMatcher("/board/**", "/board/list"));
        assertTrue(EgovAccessUtil.antMatcher("/board/*", "/board/list"));
        assertFalse(EgovAccessUtil.antMatcher("/admin/**", "/board/list"));
    }

    @Test
    void regexMatcher_behaviorUnchanged() {
        assertTrue(EgovAccessUtil.regexMatcher(".*/admin/.*", "/svc/admin/user"));
        assertTrue(EgovAccessUtil.regexMatcher("/board/.*", "/board/1"));
        assertFalse(EgovAccessUtil.regexMatcher("^/admin/.*", "/board/1"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void regexMatcher_reusesCompiledPattern() throws Exception {
        String pattern = "/cache/probe/.*";
        // 서로 다른 입력에 같은 패턴으로 두 번 호출해도 컴파일된 Pattern은 재사용되어야 한다.
        EgovAccessUtil.regexMatcher(pattern, "/cache/probe/a");
        EgovAccessUtil.regexMatcher(pattern, "/cache/probe/b");

        Field cacheField = EgovAccessUtil.class.getDeclaredField("PATTERN_CACHE");
        cacheField.setAccessible(true);
        Map<String, Pattern> cache = (Map<String, Pattern>) cacheField.get(null);

        Pattern first = cache.get(pattern);
        EgovAccessUtil.regexMatcher(pattern, "/cache/probe/c");
        Pattern second = cache.get(pattern);

        assertSame(first, second, "동일 패턴은 재컴파일 없이 캐시된 Pattern을 재사용해야 한다");
    }
}
