package org.egovframe.rte.ptl.mvc.validation;


import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordValidationTest {

	@Test
	public void testPassword1() {
		String[] notOk = { "1234567", "123456789012345678901" };
		String[] ok = { "12345678", "12345678901234567890" };
		
		for (int i = 0; i < notOk.length; i++) {
			assertFalse(RteGenericValidator.checkLength(notOk[i]));
		}
		
		for (int i = 0; i < ok.length; i++) {
			assertTrue(RteGenericValidator.checkLength(ok[i]));
		}
	}
	
	@Test
	public void testPassword2() {
		String[] notOk = { "한글패스워드입니다", "abc def", "test!@12" };
		String[] ok = { "abCD12@#", "pass#$Wo56", "tesT!@12" };
		
		for (int i = 0; i < notOk.length; i++) {
			assertFalse(RteGenericValidator.isMoreThan3CharTypeComb(notOk[i]));
		}
		
		for (int i = 0; i < ok.length; i++) {
			assertTrue(RteGenericValidator.isMoreThan3CharTypeComb(ok[i]));
		}
	}
	
	@Test
	public void testPassword3() {
		String[] notOk = { "abcaaeecc" };
		String[] ok = { "abaabbcc", };
		
		for (int i = 0; i < notOk.length; i++) {
			assertTrue(RteGenericValidator.isSeriesCharacter(notOk[i]));
		}
		
		for (int i = 0; i < ok.length; i++) {
			assertFalse(RteGenericValidator.isSeriesCharacter(ok[i]));
		}
	}
	
	@Test
	public void testPassword4() {
		String[] notOk = { "aaatest123" };
		String[] ok = { "aatest12", };
		
		for (int i = 0; i < notOk.length; i++) {
			assertTrue(RteGenericValidator.isRepeatCharacter(notOk[i]));
		}
		
		for (int i = 0; i < ok.length; i++) {
			assertFalse(RteGenericValidator.isRepeatCharacter(ok[i]));
		}
	}

}
