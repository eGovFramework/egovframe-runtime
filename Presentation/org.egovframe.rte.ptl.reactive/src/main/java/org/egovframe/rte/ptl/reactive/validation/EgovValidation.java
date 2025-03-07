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

import org.egovframe.rte.ptl.reactive.exception.EgovErrorCode;
import org.egovframe.rte.ptl.reactive.exception.EgovException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring WebFlux의 Functional Endpoints 방식에서 데이터 유효성 검증에 사용되는 클래스
 *
 * <p>Desc.: Spring WebFlux의 Functional Endpoints 방식에서 데이터 유효성 검증에 사용되는 클래스</p>
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
public class EgovValidation {

    private Validator validator;

    public EgovValidation(Validator validator) {
        this.validator = validator;
    }

    public <T> T validate(T object) {
        Set<ConstraintViolation<T>> errors = validator.validate(object);
        if (errors.isEmpty()) {
            return object;
        } else {
            String errorDetails = errors.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
            throw new EgovException(EgovErrorCode.INVALID_INPUT_VALUE, errorDetails);
        }
    }

}
