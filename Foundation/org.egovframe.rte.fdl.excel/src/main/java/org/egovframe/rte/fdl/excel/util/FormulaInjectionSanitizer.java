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
package org.egovframe.rte.fdl.excel.util;

/**
 * 셀 값의 수식 인젝션(CWE-1236)을 방지한다.
 * {@code = + - @}, 탭, CR로 시작하는 값 앞에 작은따옴표를 붙여 리터럴 문자열로 기록한다.
 */
final class FormulaInjectionSanitizer {

    private FormulaInjectionSanitizer() {
    }

    static String sanitize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        char c = text.charAt(0);
        if (c == '=' || c == '+' || c == '-' || c == '@' || c == '\t' || c == '\r') {
            return "'" + text;
        }
        return text;
    }

}
