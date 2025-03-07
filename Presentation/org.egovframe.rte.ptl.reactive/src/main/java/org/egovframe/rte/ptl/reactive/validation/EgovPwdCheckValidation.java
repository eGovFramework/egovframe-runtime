/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.rte.ptl.reactive.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 비밀번호 유효성을 검증하기 위한 클래스
 *
 * <p>Desc.: 비밀번호 유효성을 검증하기 위한 클래스</p>
 *
 * @author ESFC
 * @since 2023.08.31
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   ESFC            최초 생성
 * </pre>
 */
public class EgovPwdCheckValidation implements ConstraintValidator<EgovPwdCheck, String> {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*?])(?=\\S+$).{8,20}$");
    private static final Pattern REPETITIVE_PATTERN = Pattern.compile(".*(.)\\1{2,}.*");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 비밀번호 패턴 검증
        if (!passwordCheck(value)) {
            return false;
        }

        // 동일한 문자 반복 패턴 검증
        if (repetitivePasswordCheck(value)) {
            return false;
        }

        // 연속된 문자 패턴 검증
        if (consecutivePasswordCheck(value)) {
            return false;
        }

        return true;
    }

    /**
     * 8자 이상 20자 이하, 공백 없는 영문자, 숫자, 특수 문자(~!@#$%^&*?)의 조합 체크
     */
    public static boolean passwordCheck(String value) {
        Matcher matcher = PASSWORD_PATTERN.matcher(value);
        return matcher.matches();
    }

    /**
     * 동일한 문자열 3개 이상 반복 (ex: aaa, bbb, 111 등) 체크
     */
    public static boolean repetitivePasswordCheck(String value) {
        Matcher matcher = REPETITIVE_PATTERN.matcher(value);
        return matcher.matches();
    }

    /**
     * 연속된 문자열 3개 이상 (ex: abc, def, 123 등) 체크
     */
    public static boolean consecutivePasswordCheck(String value) {
        String tmpValue = value.toUpperCase();
        for (int i = 0; i < tmpValue.length() - 2; i++) {
            if (isConsecutive(tmpValue.charAt(i), tmpValue.charAt(i + 1), tmpValue.charAt(i + 2))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 세 개의 문자가 연속된지 확인하는 메서드
     */
    private static boolean isConsecutive(char first, char second, char third) {
        return (second - first == 1) && (third - second == 1);
    }

}
