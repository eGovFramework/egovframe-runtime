package org.egovframe.rte.ptl.mvc.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordValidationTest {

    @Test
    public void testPassword1() {
        String[] notOk = {"1234567", "123456789012345678901"};
        String[] ok = {"12345678", "12345678901234567890"};

        for (String s : notOk) {
            assertFalse(RteGenericValidator.checkLength(s));
        }

        for (String s : ok) {
            assertTrue(RteGenericValidator.checkLength(s));
        }
    }

    @Test
    public void testPassword2() {
        String[] notOk = {"한글패스워드입니다", "abc def", "test!@12"};
        String[] ok = {"abCD12@#", "pass#$Wo56", "tesT!@12"};

        for (String s : notOk) {
            assertFalse(RteGenericValidator.isMoreThan3CharTypeComb(s));
        }

        for (String s : ok) {
            assertTrue(RteGenericValidator.isMoreThan3CharTypeComb(s));
        }
    }

    @Test
    public void testPassword3() {
        String[] notOk = {"abcaaeecc"};
        String[] ok = {"abaabbcc",};

        for (String s : notOk) {
            assertTrue(RteGenericValidator.isSeriesCharacter(s));
        }

        for (String s : ok) {
            assertFalse(RteGenericValidator.isSeriesCharacter(s));
        }
    }

    @Test
    public void testPassword4() {
        String[] notOk = {"aaatest123"};
        String[] ok = {"aatest12",};

        for (String s : notOk) {
            assertTrue(RteGenericValidator.isRepeatCharacter(s));
        }

        for (String s : ok) {
            assertFalse(RteGenericValidator.isRepeatCharacter(s));
        }
    }

}
