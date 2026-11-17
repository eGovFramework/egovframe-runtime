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
     * 빈 문자열·null : 예외 없이 그대로 반환.
     */
    @Test
    public void testConvert2CamelCaseEmptyAndNull() {
        assertEquals("", CamelUtil.convert2CamelCase(""));
        assertNull(CamelUtil.convert2CamelCase(null));
    }

}
