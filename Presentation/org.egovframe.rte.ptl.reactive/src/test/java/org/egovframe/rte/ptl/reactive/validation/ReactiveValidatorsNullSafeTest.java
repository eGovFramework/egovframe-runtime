package org.egovframe.rte.ptl.reactive.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * reactive validation 패키지의 ConstraintValidator 구현이 null 입력에서
 * NullPointerException 없이 false를 반환하는지 검증한다.
 * (EgovEmailCheckValidation·EgovIPCheckValidation 등 형제 클래스는 이미 null-safe이다)
 */
class ReactiveValidatorsNullSafeTest {

    @Test
    void allValidators_nullInput_returnFalseWithoutException() {
        assertNullSafe(new EgovCnCheckValidation());
        assertNullSafe(new EgovCrnCheckValidation());
        assertNullSafe(new EgovRrnCheckValidation());
        assertNullSafe(new EgovMobilePhoneCheckValidation());
        assertNullSafe(new EgovPhoneCheckValidation());
        assertNullSafe(new EgovEnglishCheckValidation());
        assertNullSafe(new EgovKoreanCheckValidation());
        assertNullSafe(new EgovPwdCheckValidation());
    }

    private static <A extends java.lang.annotation.Annotation> void assertNullSafe(
            jakarta.validation.ConstraintValidator<A, String> validator) {
        boolean result = assertDoesNotThrow(() -> validator.isValid(null, null),
                validator.getClass().getSimpleName() + "는 null 입력에서 예외를 던지면 안 된다");
        assertFalse(result, validator.getClass().getSimpleName() + "는 null 입력에 false를 반환해야 한다");
    }

    @Test
    void validators_stillValidateNonNullInput() {
        // 가드 추가가 정상 입력 판정을 바꾸지 않는지 확인한다.
        assertFalse(new EgovEnglishCheckValidation().isValid("한글", null), "영문 검증기는 한글에 false");
        assertDoesNotThrow(() -> new EgovEnglishCheckValidation().isValid("abc", null));
        assertDoesNotThrow(() -> new EgovRrnCheckValidation().isValid("900101-1234567", null));
    }
}
