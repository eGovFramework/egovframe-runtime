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
package org.egovframe.rte.fdl.excel.util;

/**
 * 셀 값이 {@code = + - @}(또는 탭·캐리지리턴)로 시작하면 일부 스프레드시트 프로그램이 이를 수식으로
 * 해석해 실행할 수 있다(CSV/Excel Formula Injection, CWE-1236). DB에 저장된 사용자 입력을 그대로
 * 엑셀로 내보내는 화면(게시판 다운로드 등)에서 특히 위험하므로, 셀에 기록하기 전 앞에 작은따옴표를
 * 붙여 리터럴 문자열로 강제한다.
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
