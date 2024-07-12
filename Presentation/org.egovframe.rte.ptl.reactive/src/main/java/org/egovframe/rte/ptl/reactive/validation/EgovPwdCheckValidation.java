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

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean check1 = passwordCheck(value);
        if (!check1) {
            return false;
        }

        boolean check2 = repetitivePasswordCheck(value);
        if (check2) {
            return false;
        }

        boolean check3 = consecutivePasswordCheck(value);
        if (check3) {
            return false;
        }

        return true;
    }

    /**
     * 8이상 20자리 이하 자리수, 공백 체크, 영문자, 숫자, 특수 문자(~!@#$%^&*?)의 조합 체크
     */
    public static boolean passwordCheck(String value) {
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*?])(?=\\S+$).{8,20}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }

    /**
     * 동일한 문자열 3개 이상(ex: aaa, bbb, 111, etc.) 체크
     */
    public static boolean repetitivePasswordCheck(String value) {
        String regex = ".*(.)\\1{2,}.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }

    /**
     * 연속된 문자열 3개 이상(ex: abc, def, 123, etc.) 체크
     */
    public static boolean consecutivePasswordCheck(String value) {
        String tmpValue = value.toUpperCase();
        int tmpLength = tmpValue.length();
        int[] tmpArray = new int[tmpLength];
        for (int i = 0; i < tmpLength; i++) {
            tmpArray[i] = tmpValue.charAt(i);
        }

        for (int i = 0; i < tmpLength - 2; i++) {
            // 범위 A-Z / 0-9
            if ((tmpArray[i] > 47 && tmpArray[i + 2] < 58) || (tmpArray[i] > 64 && tmpArray[i + 2] < 91)) {
                if (Math.abs(tmpArray[i + 2] - tmpArray[i + 1]) == 1 && Math.abs(tmpArray[i + 2] - tmpArray[i]) == 2) {
                    return true;
                }
            }
        }

        return false;
    }

}
