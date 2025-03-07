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
 * 주민등록번호(2020년 10월 이전) 유효성을 검증하기 위한 클래스
 *
 * <p>Desc.: 주민등록번호(2020년 10월 이전) 유효성을 검증하기 위한 클래스</p>
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
public class EgovRrnCheckValidation implements ConstraintValidator<EgovRrnCheck, String> {

    private static final Pattern RRN_PATTERN = Pattern.compile("^[\\d]{6}[1-4][\\d]{6}+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String mValue = value.replaceAll("-", "");
        Matcher matcher = RRN_PATTERN.matcher(mValue);
        boolean check = matcher.find();
        if (!check) {
            return false;
        }

        int sum = 0;
        int[] weightArray = {2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5};
        for (int i = 0; i < 12; i++) {
            sum += weightArray[i] * Integer.parseInt(mValue.substring(i,i+1));
        }
        int total = 11 - sum % 11;
        if (total == 10) total = 0;
        if (total == 11) total = 1;
        return total == Integer.parseInt(mValue.substring(12));
    }

}
