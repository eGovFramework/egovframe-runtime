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
package org.egovframe.rte.ptl.reactive.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EgovMobilePhoneCheckValidation.class)
@Documented
public @interface EgovMobilePhoneCheck {

    String message() default "{validation.mobilePhoneCheck}"; // 유효성 검사 false시 반환할 기본 메시지

    Class<?>[] groups() default {}; // 어노테이션을 적용할 특정 상황(예를 들어 특정 Class 시 어노테이션 동작)

    Class<? extends Payload>[] payload() default {}; // 심각한 정도 등 메타 데이터를 정의해 넣을 수 있음

}
