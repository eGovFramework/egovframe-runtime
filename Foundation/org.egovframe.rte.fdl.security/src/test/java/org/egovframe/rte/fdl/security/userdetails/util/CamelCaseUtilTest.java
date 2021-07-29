package org.egovframe.rte.fdl.security.userdetails.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class CamelCaseUtilTest {

	@Test
	public void convert2CamelCase() {

		assertEquals("repoNameEgovframeRuntime",CamelCaseUtil.convert2CamelCase("REPO_NAME_egovframe_Runtime"));
		assertEquals("column1",CamelCaseUtil.convert2CamelCase("column1"));
		assertEquals("column1",CamelCaseUtil.convert2CamelCase("column_1"));
		assertEquals("column1Test",CamelCaseUtil.convert2CamelCase("column_1_test"));
		assertEquals("Column1Test",CamelCaseUtil.convert2CamelCase("_column_1_test_"));

	}

}