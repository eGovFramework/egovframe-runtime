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
package org.egovframe.rte.ptl.mvc.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RteGenericValidator} 미커버 public static 검증 메서드에 대한 커버리지 테스트.
 * <p>
 * 기존 {@code PasswordValidationTest} 가 다루지 않는
 * {@code isKorean}, {@code isEnglish}, {@code isHtmlTag},
 * {@code isMoreThan2CharTypeComb}, {@code isRepeatedNTimes} 를 결정적으로 검증하고,
 * {@code isValidIdIhNum} 의 정규식 가드(\d{13}) 동작을 확인한다. 소스는 변경하지 않는다.
 * </p>
 */
public class RteGenericValidatorCoverageTest {

    @Test
    public void isKoreanReturnsTrueOnlyForHangul() {
        assertTrue(RteGenericValidator.isKorean("한글"));
        assertTrue(RteGenericValidator.isKorean("가나다라"));
        // 영문/숫자/혼합은 한글이 아니다.
        assertFalse(RteGenericValidator.isKorean("abc"));
        assertFalse(RteGenericValidator.isKorean("한a"));
        assertFalse(RteGenericValidator.isKorean("한글123"));
    }

    @Test
    public void isEnglishReturnsTrueOnlyForLetters() {
        assertTrue(RteGenericValidator.isEnglish("abcDEF"));
        assertTrue(RteGenericValidator.isEnglish("Hello"));
        // 숫자/한글/공백이 섞이면 영문이 아니다.
        assertFalse(RteGenericValidator.isEnglish("abc1"));
        assertFalse(RteGenericValidator.isEnglish("ab cd"));
        assertFalse(RteGenericValidator.isEnglish("한글"));
    }

    @Test
    public void isHtmlTagReturnsFalseWhenTagPresent() {
        // 태그가 존재하면 false(=태그 미포함 아님), 없으면 true 를 반환한다.
        assertFalse(RteGenericValidator.isHtmlTag("<div>hello</div>"));
        assertFalse(RteGenericValidator.isHtmlTag("text <b> bold"));
        assertTrue(RteGenericValidator.isHtmlTag("plain text without tag"));
        // '<'...'>' 쌍이 없으면(부등호만 존재) 태그로 보지 않는다.
        assertTrue(RteGenericValidator.isHtmlTag("2 > 1 and 3 > 2"));
    }

    @Test
    public void isMoreThan2CharTypeCombRequiresLetterDigitSpecial() {
        // 영문 + 숫자 + 특수문자(~!@#$%^&*?) 조합.
        assertTrue(RteGenericValidator.isMoreThan2CharTypeComb("abc12!"));
        assertTrue(RteGenericValidator.isMoreThan2CharTypeComb("a1@"));
        // 세 종류 미충족 또는 허용되지 않는 문자(한글/공백) 포함 시 false.
        assertFalse(RteGenericValidator.isMoreThan2CharTypeComb("abcdef"));
        assertFalse(RteGenericValidator.isMoreThan2CharTypeComb("abc123"));
        assertFalse(RteGenericValidator.isMoreThan2CharTypeComb("한글1!a"));
        assertFalse(RteGenericValidator.isMoreThan2CharTypeComb("abc 1 !"));
    }

    @Test
    public void isRepeatedNTimesDetectsConsecutiveRepeats() {
        // (.)\1{n,} : 같은 문자가 최초 1 + n 회 이상 연속되면 true.
        assertTrue(RteGenericValidator.isRepeatedNTimes("aaa", 2));
        assertTrue(RteGenericValidator.isRepeatedNTimes("xy1111z", 2));
        // 반복 길이가 부족하면 false.
        assertFalse(RteGenericValidator.isRepeatedNTimes("aa", 2));
        assertFalse(RteGenericValidator.isRepeatedNTimes("ababab", 2));
    }

    @Test
    public void isValidIdIhNumRejectsMalformedByRegexGuard() {
        // \d{13} 가드: 13자리 숫자가 아니면 즉시 false.
        assertFalse(RteGenericValidator.isValidIdIhNum("123"));
        assertFalse(RteGenericValidator.isValidIdIhNum("abcdefghijklm"));
        assertFalse(RteGenericValidator.isValidIdIhNum("12345678901234"));
        // 형식은 통과해도 날짜/검증코드가 유효하지 않으면 false.
        assertFalse(RteGenericValidator.isValidIdIhNum("0000000000000"));
    }
}
