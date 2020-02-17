package egovframework.rte.ptl.mvc.validation;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordValidationTest {
	
	String[] password = {
			"1234567", "123456789012345678901", "한글패스워드입니다.", " 12345678",
			"abcdaaee", "abcaabbee", "aaaatest", "aaatesta", 
			"#!@#^#$#@", "\ttesttest"
		};
	
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
		String[] notOk = { "한글패스워드입니다", "abc def" };
		String[] ok = { "abcdefgh", "12345678", "#!@#^#$#@" };
		
		for (int i = 0; i < notOk.length; i++) {
			assertFalse(RteGenericValidator.checkCharacterType(notOk[i]));
		}
		
		for (int i = 0; i < ok.length; i++) {
			assertTrue(RteGenericValidator.checkCharacterType(ok[i]));
		}
	}
	
	@Test
	public void testPassword3() {
		String[] notOk = { "abcdaaee" };
		String[] ok = { "abcaabbee", };
		
		for (int i = 0; i < notOk.length; i++) {
			assertFalse(RteGenericValidator.checkSeries(notOk[i]));
		}
		
		for (int i = 0; i < ok.length; i++) {
			assertTrue(RteGenericValidator.checkSeries(ok[i]));
		}
	}
	
	@Test
	public void testPassword4() {
		String[] notOk = { "aaaatest" };
		String[] ok = { "aaatesta", };
		
		for (int i = 0; i < notOk.length; i++) {
			assertFalse(RteGenericValidator.checkSeries(notOk[i]));
		}
		
		for (int i = 0; i < ok.length; i++) {
			assertTrue(RteGenericValidator.checkSeries(ok[i]));
		}
	}

}
