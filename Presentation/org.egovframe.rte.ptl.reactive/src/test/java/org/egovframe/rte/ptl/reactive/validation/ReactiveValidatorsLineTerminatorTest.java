package org.egovframe.rte.ptl.reactive.validation;

import jakarta.validation.ConstraintValidator;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 행종결자로 끝나는 입력을 예외 없이 false로 반환하는지 검증한다.
 *
 * <p>대상은 정규식 검사를 matches()로 전환한 검증기 6종(법인등록번호·주민등록번호·
 * 휴대전화번호·일반전화번호·영문·한국어)이다. 이미 matches()를 사용하는
 * EgovCrnCheckValidation·EgovPwdCheckValidation·EgovEmailCheckValidation·
 * EgovIPCheckValidation은 이 테스트의 대상이 아니다.</p>
 */
class ReactiveValidatorsLineTerminatorTest {

    @Test
    void validators_lineTerminatorInput_returnFalseWithoutException() {
        assertAll(
                () -> assertLineTerminatorRejected(new EgovCnCheckValidation(), "1101110000002"),
                () -> assertLineTerminatorRejected(new EgovRrnCheckValidation(), "9913321123459"),
                () -> assertLineTerminatorRejected(new EgovMobilePhoneCheckValidation(), "01012345678"),
                () -> assertLineTerminatorRejected(new EgovPhoneCheckValidation(), "0212345678"),
                () -> assertLineTerminatorRejected(new EgovEnglishCheckValidation(), "abc"),
                () -> assertLineTerminatorRejected(new EgovKoreanCheckValidation(), "가나다"));
    }

    @Test
    void cnAndRrn_lineFeedInput_doNotThrowNumberFormatException() {
        assertFalse(assertDoesNotThrow(() -> new EgovCnCheckValidation().isValid("1101110000002\n", null),
                "법인등록번호 검증기는 개행 입력에서 NumberFormatException을 전파하면 안 된다"),
                "법인등록번호 검증기는 개행으로 끝나는 입력에 false를 반환해야 한다");
        assertFalse(assertDoesNotThrow(() -> new EgovRrnCheckValidation().isValid("9913321123459\n", null),
                "주민등록번호 검증기는 개행 입력에서 NumberFormatException을 전파하면 안 된다"),
                "주민등록번호 검증기는 개행으로 끝나는 입력에 false를 반환해야 한다");
    }

    @Test
    void cn_unicodeLineSeparatorInput_returnFalseWithoutException() {
        boolean result = assertDoesNotThrow(() -> new EgovCnCheckValidation().isValid("1101110000002\u2028", null),
                "법인등록번호 검증기는 유니코드 행종결자 입력에서 예외를 던지면 안 된다");
        assertFalse(result, "법인등록번호 검증기는 유니코드 행종결자로 끝나는 입력에 false를 반환해야 한다");
    }

    @Test
    void validators_stillValidateNormalInput() {
        assertTrue(new EgovCnCheckValidation().isValid("1101110000002", null), "법인등록번호 검증기는 정상값에 true");
        assertTrue(new EgovCnCheckValidation().isValid("110111-0000002", null), "법인등록번호 검증기는 하이픈 포함 정상값에 true");
        assertFalse(new EgovCnCheckValidation().isValid("1101110000003", null), "법인등록번호 검증기는 체크숫자 오류에 false");

        assertTrue(new EgovRrnCheckValidation().isValid("9913321123459", null), "주민등록번호 검증기는 정상값에 true");
        assertTrue(new EgovRrnCheckValidation().isValid("991332-1123459", null), "주민등록번호 검증기는 하이픈 포함 정상값에 true");
        assertFalse(new EgovRrnCheckValidation().isValid("9913321123458", null), "주민등록번호 검증기는 체크숫자 오류에 false");

        assertTrue(new EgovMobilePhoneCheckValidation().isValid("01012345678", null), "휴대전화번호 검증기는 정상값에 true");
        assertTrue(new EgovMobilePhoneCheckValidation().isValid("010-1234-5678", null), "휴대전화번호 검증기는 하이픈 포함 정상값에 true");
        assertFalse(new EgovMobilePhoneCheckValidation().isValid("01512345678", null), "휴대전화번호 검증기는 잘못된 식별번호에 false");

        assertTrue(new EgovPhoneCheckValidation().isValid("0212345678", null), "일반전화번호 검증기는 정상값에 true");
        assertTrue(new EgovPhoneCheckValidation().isValid("02-1234-5678", null), "일반전화번호 검증기는 하이픈 포함 정상값에 true");
        assertFalse(new EgovPhoneCheckValidation().isValid("021234567890", null), "일반전화번호 검증기는 길이 오류에 false");

        assertTrue(new EgovEnglishCheckValidation().isValid("abc", null), "영문 검증기는 소문자에 true");
        assertTrue(new EgovEnglishCheckValidation().isValid("ABC", null), "영문 검증기는 대문자에 true");
        assertFalse(new EgovEnglishCheckValidation().isValid("abc1", null), "영문 검증기는 숫자 포함 값에 false");
        assertFalse(new EgovEnglishCheckValidation().isValid("한글", null), "영문 검증기는 한글에 false");

        assertTrue(new EgovKoreanCheckValidation().isValid("가나다", null), "한국어 검증기는 완성형 한글에 true");
        assertTrue(new EgovKoreanCheckValidation().isValid("ㄱㄴㄷ", null), "한국어 검증기는 한글 자모에 true");
        assertFalse(new EgovKoreanCheckValidation().isValid("가나1", null), "한국어 검증기는 숫자 포함 값에 false");
        assertFalse(new EgovKoreanCheckValidation().isValid("abc", null), "한국어 검증기는 영문에 false");
    }

    private static <A extends Annotation> void assertLineTerminatorRejected(
            ConstraintValidator<A, String> validator, String validValue) {
        assertRejected(validator, validValue + "\n");
        assertRejected(validator, validValue + "\r\n");
    }

    private static <A extends Annotation> void assertRejected(
            ConstraintValidator<A, String> validator, String value) {
        boolean result = assertDoesNotThrow(() -> validator.isValid(value, null),
                validator.getClass().getSimpleName() + "는 행종결자 입력에서 예외를 던지면 안 된다");
        assertFalse(result, validator.getClass().getSimpleName() + "는 행종결자로 끝나는 입력에 false를 반환해야 한다");
    }

}
