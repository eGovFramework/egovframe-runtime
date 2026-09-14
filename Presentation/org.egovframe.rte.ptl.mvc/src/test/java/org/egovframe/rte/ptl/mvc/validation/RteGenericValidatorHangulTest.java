package org.egovframe.rte.ptl.mvc.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RteGenericValidator.isHangul 의 순수 한글 판정과 isKorean 과의 차이(다른 CJK 문자 통과)를 검증한다.
 */
public class RteGenericValidatorHangulTest {

    @Test
    public void testHangulSyllablesAndJamoAreAccepted() {
        assertTrue(RteGenericValidator.isHangul("한글"));
        assertTrue(RteGenericValidator.isHangul("가나다라"));
        assertTrue(RteGenericValidator.isHangul("ㄱㄴㄷ"));
        assertTrue(RteGenericValidator.isHangul("ㅏㅑㅓ"));
        assertTrue(RteGenericValidator.isHangul("홍ㄱ길동ㅏ"));
    }

    @Test
    public void testOtherCjkLettersAreRejectedByIsHangulButPassIsKorean() {
        assertTrue(RteGenericValidator.isKorean("漢字"));
        assertFalse(RteGenericValidator.isHangul("漢字"));

        assertTrue(RteGenericValidator.isKorean("ひらがな"));
        assertFalse(RteGenericValidator.isHangul("ひらがな"));

        assertTrue(RteGenericValidator.isKorean("한글漢字"));
        assertFalse(RteGenericValidator.isHangul("한글漢字"));
    }

    @Test
    public void testAsciiDigitsSpacesAndMixedInputAreRejected() {
        assertFalse(RteGenericValidator.isHangul("abc"));
        assertFalse(RteGenericValidator.isHangul("한a"));
        assertFalse(RteGenericValidator.isHangul("한글123"));
        assertFalse(RteGenericValidator.isHangul("한 글"));
        assertFalse(RteGenericValidator.isHangul("한글!"));
    }

    @Test
    public void testEmptyIsTrueLikeIsKoreanAndNullIsFalse() {
        assertTrue(RteGenericValidator.isKorean(""));
        assertTrue(RteGenericValidator.isHangul(""));
        assertFalse(RteGenericValidator.isHangul(null));
    }

}
