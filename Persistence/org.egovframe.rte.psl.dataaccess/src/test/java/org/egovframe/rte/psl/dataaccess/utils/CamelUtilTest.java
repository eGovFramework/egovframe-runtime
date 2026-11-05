package org.egovframe.rte.psl.dataaccess.utils;

import org.egovframe.rte.psl.dataaccess.util.CamelUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

}
