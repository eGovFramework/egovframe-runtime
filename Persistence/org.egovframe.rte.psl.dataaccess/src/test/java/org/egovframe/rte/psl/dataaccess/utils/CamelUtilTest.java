package org.egovframe.rte.psl.dataaccess.utils;

import static org.junit.Assert.*;

import org.egovframe.rte.psl.dataaccess.util.CamelUtil;
import org.junit.Test;

public class CamelUtilTest {

	@Test
	public void testConvert2CamelCase() {
		String camelString = "camelString";
		String notCamelString = "not_camelString";
		String upperCaseString = "Uppercase";
		
		assertEquals(camelString,CamelUtil.convert2CamelCase(camelString));
		assertEquals("notCamelstring",CamelUtil.convert2CamelCase(notCamelString));
		assertEquals("uppercase",CamelUtil.convert2CamelCase(upperCaseString));
	}

}
