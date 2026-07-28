package org.egovframe.rte.psl.dataaccess.utils;

import org.egovframe.rte.psl.dataaccess.util.CamelUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CamelUtilTest {

    @Test
    public void testConvert2CamelCase() {
        String camelString = "camelString";
        String notCamelString = "not_camelString";
        String upperCaseString = "Uppercase";

        assertEquals(camelString, CamelUtil.convert2CamelCase(camelString));
        assertEquals("notCamelstring", CamelUtil.convert2CamelCase(notCamelString));
        assertEquals("uppercase", CamelUtil.convert2CamelCase(upperCaseString));
    }

    /**
     * 빈 문자열은 charAt(0)에서, null은 indexOf 호출에서 예외가 발생하던 것을
     * 그대로 반환하도록 가드를 추가했다.
     */
    @Test
    public void testConvert2CamelCaseEmptyAndNull() {
        assertEquals("", CamelUtil.convert2CamelCase(""));
        assertNull(CamelUtil.convert2CamelCase(null));
    }

}
