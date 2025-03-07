package org.egovframe.rte.ptl.reactive.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EgovKoreanCheckValidation.class)
@Documented
public @interface EgovKoreanCheck {

    String message() default "Invalid input values"; // 유효성 검사 false시 반환할 기본 메시지
    Class<?>[] groups() default { }; // 어노테이션을 적용할 특정 상황(예를 들어 특정 Class 시 어노테이션 동작)
    Class<? extends Payload>[] payload() default { }; // 심각한 정도 등 메타 데이터를 정의해 넣을 수 있음

}
