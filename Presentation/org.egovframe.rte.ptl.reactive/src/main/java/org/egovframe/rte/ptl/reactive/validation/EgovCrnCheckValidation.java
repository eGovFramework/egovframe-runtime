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
 * 사업자등록번호 유효성을 검증하기 위한 클래스
 *
 * <p>Desc.: 사업자등록번호 유효성을 검증하기 위한 클래스</p>
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
public class EgovCrnCheckValidation implements ConstraintValidator<EgovCrnCheck, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String mValue = value.replaceAll("-", "");
        String regex = "^[\\d]{10}+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mValue);
        boolean check = matcher.find();
        if (!check) {
            return false;
        }

        int sum = 0;
        int[] weightArray = {1, 3, 7, 1, 3, 7, 1, 3, 5, 1};
        for (int i = 0; i < 9; i++) {
            sum += weightArray[i] * Integer.parseInt(mValue.substring(i,i+1));
        }
        sum += Integer.parseInt(mValue.substring(8,9)) * 5 / 10;
        int total = (10 - sum % 10) % 10;
        if (total == Integer.parseInt(mValue.substring(9))) {
            return true;
        } else {
            return false;
        }
    }

}
