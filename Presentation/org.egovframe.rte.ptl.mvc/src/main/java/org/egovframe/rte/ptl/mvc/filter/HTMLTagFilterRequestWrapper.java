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
package org.egovframe.rte.ptl.mvc.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * HTMLTagFilterRequestWrapper.java
 *
 * <p>요청 파라미터의 HTML 특수문자를 엔티티로 인코딩하는 래퍼. 기본 동작(인자 없는 생성자)은
 * 모든 {@code < > & " '} 를 인코딩하며, {@link HTMLTagFilter}의 초기화 파라미터로
 * <b>허용 태그</b>(속성 없는 {@code <p>}·{@code </p>}·{@code <br/>} 형태만 통과)와
 * <b>리치텍스트 우회 파라미터</b>(인코딩 전체 우회 — 출력 시점에 {@code EgovHtmlSanitizer}
 * 정제가 전제)를 지정할 수 있다.</p>
 *
 * @author 실행환경 개발팀 함철
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	함철				최초 생성
 * 2026.09.01	실행환경 개발팀		허용 태그·리치텍스트 우회 목록 지원(공통컴포넌트
 *                              분기 기능을 하드코딩 없이 설정형으로 승격 — & 인코딩 유지,
 *                              사본과 달리 원본 배열·맵은 변조하지 않는다)
 * </pre>
 * @since 2009.06.01
 */
public class HTMLTagFilterRequestWrapper extends HttpServletRequestWrapper {

    /** 인코딩하지 않고 통과시킬 허용 태그 이름(소문자) — 속성이 붙으면 통과하지 않는다. */
    private final Set<String> allowedTagNames;

    /** 인코딩을 통째로 우회하는 리치텍스트 파라미터명. */
    private final Set<String> richTextParameterNames;

    public HTMLTagFilterRequestWrapper(HttpServletRequest request) {
        this(request, Collections.emptySet(), Collections.emptySet());
    }

    /**
     * 허용 태그·리치텍스트 우회 목록을 받는 생성자. 두 목록이 비어 있으면
     * 기존 생성자와 동작이 완전히 같다.
     *
     * @param request                요청
     * @param allowedTagNames        허용 태그 이름 목록(예: {@code p}, {@code br} — 대소문자 무관)
     * @param richTextParameterNames 인코딩을 우회할 파라미터명 목록
     */
    public HTMLTagFilterRequestWrapper(HttpServletRequest request, Set<String> allowedTagNames,
            Set<String> richTextParameterNames) {
        super(request);
        this.allowedTagNames = lowerCased(allowedTagNames);
        this.richTextParameterNames = (richTextParameterNames == null || richTextParameterNames.isEmpty())
                ? Collections.emptySet()
                : new LinkedHashSet<String>(richTextParameterNames);
    }

    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
        if (isRichTextParameter(parameter)) {
            return values.clone();
        }
        String[] safeValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                safeValues[i] = getSafeParamData(values[i]);
            } else {
                safeValues[i] = null;
            }
        }
        return safeValues;
    }

    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        if (value == null) {
            return null;
        }
        if (isRichTextParameter(parameter)) {
            return value;
        }
        value = getSafeParamData(value);
        return value;
    }

    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> valueMap = super.getParameterMap();
        Map<String, String[]> safeValueMap = new LinkedHashMap<String, String[]>();
        for (String key : valueMap.keySet()) {
            String[] values = valueMap.get(key);
            if (isRichTextParameter(key)) {
                safeValueMap.put(key, (values == null) ? null : values.clone());
                continue;
            }
            String[] safeValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    safeValues[i] = getSafeParamData(values[i]);
                } else {
                    safeValues[i] = null;
                }
            }
            safeValueMap.put(key, safeValues);
        }
        return safeValueMap;
    }

    /**
     * XSS 방지를 위해 파라미터 값을 HTML 엔티티로 이스케이프.
     * null 입력 시 null 반환 (NPE 방지).
     *
     * <p>허용 태그가 설정된 경우, 속성 없는 여닫는 형태({@code <p>}·{@code </p>}·{@code <br/>}·
     * {@code <br />})만 원문 그대로 통과한다 — 속성이 하나라도 붙으면({@code <p onclick=…>})
     * 통과하지 않는다.</p>
     */
    public String getSafeParamData(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '<':
                    int tagEnd = matchAllowedTag(value, i);
                    if (tagEnd >= 0) {
                        stringBuilder.append(value, i, tagEnd + 1);
                        i = tagEnd;
                    } else {
                        stringBuilder.append("&lt;");
                    }
                    break;
                case '>':
                    stringBuilder.append("&gt;");
                    break;
                case '&':
                    stringBuilder.append("&amp;");
                    break;
                case '"':
                    stringBuilder.append("&quot;");
                    break;
                case '\'':
                    stringBuilder.append("&apos;");
                    break;
                default:
                    stringBuilder.append(c);
                    break;
            }
        }
        value = stringBuilder.toString();
        return value;
    }

    private boolean isRichTextParameter(String parameter) {
        return !richTextParameterNames.isEmpty() && richTextParameterNames.contains(parameter);
    }

    /**
     * {@code ltIndex}({@code '<'} 위치)에서 시작하는 부분이 허용 태그의 온전한 여닫는 형태이면
     * 닫는 {@code '>'} 의 인덱스를, 아니면 -1 을 반환한다. 문자열 끝에 걸친 태그도 정상 판정한다
     * (공통컴포넌트 사본은 끝 경계에서 {@code &lt;p>} 혼종을 만드는 버그가 있었다).
     */
    private int matchAllowedTag(String value, int ltIndex) {
        if (allowedTagNames.isEmpty()) {
            return -1;
        }
        int length = value.length();
        int i = ltIndex + 1;
        boolean closing = (i < length && value.charAt(i) == '/');
        if (closing) {
            i++;
        }
        int nameStart = i;
        while (i < length && isAsciiLetter(value.charAt(i))) {
            i++;
        }
        if (i == nameStart) {
            return -1;
        }
        String name = value.substring(nameStart, i).toLowerCase(Locale.ROOT);
        if (!allowedTagNames.contains(name)) {
            return -1;
        }
        while (i < length && value.charAt(i) == ' ') {
            i++;
        }
        if (!closing && i < length && value.charAt(i) == '/') {
            i++;
        }
        return (i < length && value.charAt(i) == '>') ? i : -1;
    }

    private static boolean isAsciiLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static Set<String> lowerCased(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> lowered = new LinkedHashSet<String>();
        for (String name : names) {
            if (name != null && !name.isEmpty()) {
                lowered.add(name.toLowerCase(Locale.ROOT));
            }
        }
        return lowered;
    }

}
