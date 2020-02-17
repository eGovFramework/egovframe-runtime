package egovframework.rte.fdl.string;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * @author sjyoon
 *
 */
public class EgovStringUtilTest {

	/**
	 * String이 특정 Pattern(정규표현식)에 부합하는지 검사한다.
	 * @throws Exception
	 */
	@Test
	public void testPatternMatch() throws Exception {
		String source = "2009-02-3";
		String pattern = "\\d{4}-\\d{1,2}-\\d{1,2}";
		assertTrue(EgovStringUtil.isPatternMatch(source, pattern));

		// pattern match 성공
		String str = "abc-def";
		pattern = "*-*";
		assertTrue(EgovStringUtil.isPatternMatching(str, pattern));

		// pattern match 실패
		str = "abc";
		assertTrue(!EgovStringUtil.isPatternMatching(str, pattern));
	}

	/**
	 * 전체 String 중 일부를 가져온다.
	 * @throws Exception
	 */
	@Test
	public void testToSubString() throws Exception {
		String source = "substring test";

		assertEquals("test", EgovStringUtil.toSubString(source, 10));
		assertEquals("string", EgovStringUtil.toSubString(source, 3, 9));
	}

	/**
	 * 전체 String 중 앞뒤에 존재하는 공백 문자(white character)를 제거한다.
	 * @throws Exception
	 */
	@Test
	public void testStringTrim() throws Exception {
		String str = "  substring  ";

		assertEquals("substring", EgovStringUtil.trim(str));
		assertEquals("substring  ", EgovStringUtil.ltrim(str));
		assertEquals("  substring", EgovStringUtil.rtrim(str));
	}

	/**
	 * 두 String을 붙여서 하나의 String을 생성한다.
	 * @throws Exception
	 */
	@Test
	public void testConcatenate() throws Exception {

		String str1 = "substring";
		String str2 = "test";

		assertEquals("substringtest", EgovStringUtil.concat(str1, str2));
	}

	/**
	 * 전체 String 중 특정 String Pattern이 있는지 찾는다.
	 * @throws Exception
	 */
	@Test
	public void testFindPattern() throws Exception {
		String pattern = "\\d{4}-\\d{1,2}-\\d{1,2}";

		// 일치하는 pattern 을 찾는다.
    	Matcher matcher = Pattern.compile(pattern).matcher("2009-02-03");
    	assertTrue(matcher.find());
    	assertTrue(matcher.matches());

    	// 일치하는 pattern 을 찾는다.
    	matcher = Pattern.compile(pattern).matcher("abcdef2009-02-03abcdef");
    	assertTrue(matcher.find());
    	assertFalse(matcher.matches());

    	// 일치하는 pattern 을 찾지 못한다.
    	matcher = Pattern.compile(pattern).matcher("abcdef2009-02-A3abcdef");
    	assertFalse(matcher.find());
    	assertFalse(matcher.matches());
	}

	/**
	 * string 이 null 인지 검사한다.
	 * @throws Exception
	 */
	@Test
	public void testIsNull() throws Exception {
		// 1. check empty string
		assertTrue(EgovStringUtil.isNull(""));
		// 2. check null
		assertTrue(EgovStringUtil.isNull(null));
		// 3. check not null
		assertFalse(EgovStringUtil.isNull("string"));
	}

	/**
	 * string이 알파벳인지 검사한다.
	 * @throws Exception
	 */
	@Test
	public void testIsAlpha() throws Exception {
		// 1. string is empty
		String str = "";
		assertTrue(!EgovStringUtil.isAlpha(str));
		// 2. string is null
		str = null;
		assertTrue(!EgovStringUtil.isAlpha(str));
		// 3. strigng consist of only alphabet
		str = "abc";
		assertTrue(EgovStringUtil.isAlpha(str));
		// 4. string has a special character
		str = "a-bc";
		assertTrue(!EgovStringUtil.isAlpha(str));
		// 5. strigng consist of alphabet and number
		str = "abc4";
		assertTrue(!EgovStringUtil.isAlpha(str));
	}

	/**
	 * string이 알파벳 숫자식인지 검사한다.
	 * @throws Exception
	 */
	@Test
	public void testIsAlphaNumeric() throws Exception {
		// 1. string is empty
		String str = "";
		assertTrue(!EgovStringUtil.isAlphaNumeric(str));
		// 2. string is null
		str = null;
		assertTrue(!EgovStringUtil.isAlphaNumeric(str));
		// 3. strigng consist of only alphabet
		str = "abc";
		assertTrue(EgovStringUtil.isAlphaNumeric(str));
		// 4. string has a special character
		str = "a-bc";
		assertTrue(!EgovStringUtil.isAlphaNumeric(str));
		// 5. strigng consist of alphabet and number
		str = "abc4";
		assertTrue(EgovStringUtil.isAlphaNumeric(str));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testNull2void() throws Exception {
		String source = null;

		assertEquals("", EgovStringUtil.null2void(source));

		source = " ";
		assertEquals("", EgovStringUtil.null2void(source));

		source = "";
		assertEquals("", EgovStringUtil.null2void(source));
	}


	/**
	 * 다양한 형식의 string을 다른 형식으로 변환한다.
	 * 다양한 형식의 데이터를 String 형식으로 변환한다.
	 * @throws Exception
	 */
	@Test
	public void testTypeConversion() throws Exception {
		// int => string
		assertEquals("1", EgovStringUtil.integer2string(1));

		// long => string
		assertEquals("1000000000", EgovStringUtil.long2string(1000000000));

		// float => string
		assertEquals("34.5", EgovStringUtil.float2string(34.5f));

		// double => string
		assertEquals("34.5", EgovStringUtil.double2string(34.5));

		// string => int
		assertEquals(1, EgovStringUtil.string2integer("1"));
		assertEquals(0, EgovStringUtil.string2integer(null, 0));

		// string => float
		assertEquals(Float.valueOf(34.5f), Float.valueOf(EgovStringUtil.string2float("34.5")));
		assertEquals(Float.valueOf(10.5f), Float.valueOf(EgovStringUtil.string2float(null, 10.5f)));

		// string => double
		assertEquals(Double.valueOf(34.5), Double.valueOf(EgovStringUtil.string2double("34.5")));
		assertEquals(Double.valueOf(34.5), Double.valueOf(EgovStringUtil.string2double(null, 34.5)));

		// string => long
		assertEquals(100000000, EgovStringUtil.string2long("100000000"));
		assertEquals(100000000, EgovStringUtil.string2long(null, 100000000));
	}

	/**
	 * @throws Exception
	 */
	public void testNull2string1() throws Exception {
		String string = null;
		assertEquals("", EgovStringUtil.null2string(string, ""));
	}


	public void testEquals(String source, String target) throws Exception {
		EgovStringUtil.equals(source, target);
	}



	public void testSearch() throws Exception {
		String source = "substring test";
		String target = "test";
		int count = 1;

		assertEquals(count, EgovStringUtil.search(source, target));
	}


	/**
	 * @throws Exception
	 */
	@Test
	public void testLPad() throws Exception {
		String source = "test ";
		int i = 10;
		char c = 'a';

		String result = EgovStringUtil.lPad(source, i, c);

		assertEquals("aaaaatest ", result);

		result = EgovStringUtil.lPad(source, i, c, true);
		assertEquals("aaaaaatest", result);


		source = " test";

		result = EgovStringUtil.rPad(source, i, c);

		assertEquals(" testaaaaa", result);
		result = EgovStringUtil.rPad(source, i, c, true);

		assertEquals("testaaaaaa", result);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testAlignString() throws Exception {
		String source = "align test";
		String result = "";
		int i = 15;

		// align Left
		result = EgovStringUtil.alignLeft(source, i);
		assertEquals("align test     ", result);
		assertEquals(i, result.length());

		i = 5;
		result = EgovStringUtil.alignLeft(source, i);
		assertEquals("align", result);
		assertEquals(i, result.length());

		// align Right
		i = 15;
		result = EgovStringUtil.alignRight(source, i);
		assertEquals("     align test", result);
		assertEquals(i, result.length());

		i = 5;
		result = EgovStringUtil.alignRight(source, i);
		assertEquals("align", result);
		assertEquals(i, result.length());
	}
	/*
	public void testAlignLeft() throws Exception {
		String source;
		int i;
		boolean b;

		EgovStringUtil.alignLeft(source, i, b);
	}

	public void testAlignRight() throws Exception {
		String source;
		int i;

		EgovStringUtil.alignRight(source, i);
	}

	public void testAlignRight() throws Exception {
		String source;
		int i;
		boolean b;
		EgovStringUtil.alignRight(source, i, b);
	}

	public void testAlignCenter() throws Exception {
		String source;
		int i;

		EgovStringUtil.alignCenter(source, i);
	}

	public void testAlignCenter() throws Exception {
		String source;
		int i;
		boolean b;

		EgovStringUtil.alignCenter(source, i, b);
	}

	public void testCapitalize() throws Exception {
		String source;

		EgovStringUtil.capitalize(source);
	}
*/

	/**
	 * @throws Exception
	 */
	public void testEncodePassword() throws Exception {
		// 1. try to encode password and compare
		String encoded1 = EgovStringUtil.encodePassword("password", "MD5");
		String encoded2 = EgovStringUtil.encodePassword("password", "MD5");
		assertEquals(encoded1, encoded2);
		// 2. define not available algorithm 'MD6 MessageDigest'
		String encoded3 = EgovStringUtil.encodePassword("password", "MD6");
		assertEquals("password", encoded3);
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public void testEncodeString() throws Exception {
		// 1. try to encode string and decode that.
		String encoded = EgovStringUtil.encodeString("password");
		String decoded = EgovStringUtil.decodeString(encoded);
		assertEquals("password", decoded);
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public void testDecodeString() throws Exception {
		// 1. try to encode password and compare decoded string and original
		// string
		String encoded = EgovStringUtil.encodeString("password");
		String decoded = EgovStringUtil.decodeString(encoded);
		assertEquals("password", decoded);
	}

	/**
	 * @throws Exception
	 */
	public void testSwapFirstLetterCase() throws Exception {
		// 1. In case, first letter is small letter. try to swap.
		String swapped = EgovStringUtil.swapFirstLetterCase("password");
		assertEquals("Password", swapped);
		// 2. In case, first letter is big letter. try to swap.
		swapped = EgovStringUtil.swapFirstLetterCase("PASSWORD");
		assertEquals("pASSWORD", swapped);
	}

	/**
	 * @throws Exception
	 */
	public void testTrim() throws Exception {
		// 1. try to trim when trimmed string is 'trim'
		String trimmed = EgovStringUtil.trim("passwordtrimpassword", "trim");
		assertEquals("passwordpassword", trimmed);
		// 2. try to trim when trimmed string is ','
		trimmed = EgovStringUtil.trim("passwordtrimpassword", ",");
		assertEquals("passwordtrimpassword", trimmed);
	}

	/**
	 * @throws Exception
	 */
	public void testGetLastString() throws Exception {
		// 1. get last string when token is ','
		String trimmed = EgovStringUtil.getLastString("password,password", ",");
		assertEquals("password", trimmed);

		// 2. get last string when original doesn't have token.
		trimmed = EgovStringUtil.getLastString("password,password", "*");
		assertEquals("password,password", trimmed);
	}

	/**
	 * @throws Exception
	 */
	public void testGetStringArray() throws Exception {
		// 1. when original string has token, get string array.
		String[] strings = EgovStringUtil.getStringArray("password,password", ",");
		assertEquals(2, strings.length);
		// 2. when original string doesn't have token, get string array.
		strings = EgovStringUtil.getStringArray("password", ",");
		assertEquals(1, strings.length);
	}

	/**
	 * @throws Exception
	 */
	public void testIsNotEmpty() throws Exception {
		// 1. check which string is not empty or not
		assertTrue(EgovStringUtil.isNotEmpty("passwordtrimpassword"));
	}

	/**
	 * @throws Exception
	 */
	public void testIsEmpty() throws Exception {
		// 1. check empty string
		assertTrue(EgovStringUtil.isEmpty(""));
		// 2. check null
		assertTrue(EgovStringUtil.isEmpty(null));
	}

	/**
	 * @throws Exception
	 */
	public void testReplace() throws Exception {
		// 1. try to replace ',' to '-'
		String replaced = EgovStringUtil.replace("password,password", ",", "-");
		assertEquals("password-password", replaced);
	}

	/**
	 * converts the string representation of a
	 * number to integer type
	 *
	public void testString2integer() {
		// 1. converts the string representation of a number to integer type
		assertEquals(1, StringUtil.string2integer("1"));
	}

	/**
	 * converts integer type to String
	 *
	public void testInteger2string() {
		// 1. converts integer type to String
		assertEquals("1", StringUtil.integer2string(1));
	}

	/**
	 * check that str matches the pattern
	 * string
	 *
	 * @throws Exception
	 *             fail to test
	 */
	public void testIsPatternMatching() throws Exception {
		// 1. str matches the pattern
		String str = "abc-def";
		String pattern = "*-*";
		assertTrue(EgovStringUtil.isPatternMatching(str, pattern));
		// 2. str doesn't match the pattern
		str = "abc";
		assertTrue(!EgovStringUtil.isPatternMatching(str, pattern));
	}

	/**
	 * @throws Exception
	 */
	public void testContainsMaxSequence() throws Exception {
		// 1. string contains 2 sequences of the same character
		String str = "password";
		String maxSeqNumber = "2";
		assertTrue(EgovStringUtil.containsMaxSequence(str, maxSeqNumber));
		// 2. string contains 3 sequences of the same character
		str = "my000";
		maxSeqNumber = "3";
		assertTrue(EgovStringUtil.containsMaxSequence(str, maxSeqNumber));
		// 3. string doesn't contain any sequence of the same character
		str = "abbbbc";
		maxSeqNumber = "5";
		assertTrue(!EgovStringUtil.containsMaxSequence(str, maxSeqNumber));
		// 4. string is null
		str = null;
		assertTrue(!EgovStringUtil.containsMaxSequence(str, maxSeqNumber));
	}

	/**
	 * @throws Exception
	 */
	public void testContainsInvalidChars() throws Exception {
		// 1. string is empty.
		String str = "";
		char[] invalidChars = new char[] { '*', '%' };
		assertTrue(!EgovStringUtil.containsInvalidChars(str, invalidChars));
		// 2. string is null.
		str = null;
		assertTrue(!EgovStringUtil.containsInvalidChars(str, invalidChars));
		// 3. invalid chars doesn't defined.
		str = "";
		assertTrue(!EgovStringUtil.containsInvalidChars(str, new char[] {}));
		// 4. string has invalid chars.
		str = "x*yz";
		assertTrue(EgovStringUtil.containsInvalidChars(str, invalidChars));
		assertTrue(EgovStringUtil.containsInvalidChars(str, "yz"));
	}

	/**
	 * [Flow #-16] Positive, Negative Case : check that String contains only
	 * unicode letters or digits
	 *
	public void testIsAlphaNumeric() {
		// 1. string is empty
		String str = "";
		assertTrue(!StringUtil.isAlphaNumeric(str));
		// 2. string is null
		str = null;
		assertTrue(!StringUtil.isAlphaNumeric(str));
		// 3. strigng consist of only alphabet
		str = "abc";
		assertTrue(StringUtil.isAlphaNumeric(str));
		// 4. string has a special character
		str = "a-bc";
		assertTrue(!StringUtil.isAlphaNumeric(str));
		// 5. strigng consist of alphabet and number
		str = "abc4";
		assertTrue(StringUtil.isAlphaNumeric(str));
	}

	/**
	 * [Flow #-17] Positive, Negative Case : check that String contains only
	 * unicode letters
	 *
	public void testIsAlpha() {
		// 1. string is empty
		String str = "";
		assertTrue(!StringUtil.isAlpha(str));
		// 2. string is null
		str = null;
		assertTrue(!StringUtil.isAlpha(str));
		// 3. strigng consist of only alphabet
		str = "abc";
		assertTrue(StringUtil.isAlpha(str));
		// 4. string has a special character
		str = "a-bc";
		assertTrue(!StringUtil.isAlpha(str));
		// 5. strigng consist of alphabet and number
		str = "abc4";
		assertTrue(!StringUtil.isAlpha(str));
	}
*/
	/**
	 * @throws Exception
	 */
	public void testIsNumeric() throws Exception {
		// 1. string is empty
		String str = "";
		assertTrue(!EgovStringUtil.isNumeric(str));
		// 2. string is null
		str = null;
		assertTrue(!EgovStringUtil.isNumeric(str));
		// 3. strigng consist of only alphabet
		str = "abc";
		assertTrue(!EgovStringUtil.isNumeric(str));
		// 4. string has a special character
		str = "a-bc";
		assertTrue(!EgovStringUtil.isNumeric(str));
		// 5. strigng consist of alphabet and number
		str = "abc4";
		assertTrue(!EgovStringUtil.isNumeric(str));
		// 5. strigng consist of only number
		str = "1234";
		assertTrue(EgovStringUtil.isNumeric(str));
	}

	/**
	 * @throws Exception
	 */
	public void testReverse() throws Exception {
		// 1. string is null
		String str = null;
		assertNull(EgovStringUtil.reverse(str));
		// 1. string is 'bat'
		str = "bat";
		assertEquals("tab", EgovStringUtil.reverse(str));
	}

	/**
	 * @throws Exception
	 */
	public void testFillString() throws Exception {
		String originalStr = "1";
		char ch = '0';
		int cipers = 6;
		assertEquals("000001", EgovStringUtil.fillString(originalStr, ch, cipers));

		originalStr = "12345";
		cipers = 4;
		assertNull(EgovStringUtil.fillString(originalStr, ch, cipers));

	}

	/**
	 * @throws Exception
	 */
	public void testIsEmptyTrimmed() throws Exception {
		// 1. string is null
		String str = null;
		assertTrue(EgovStringUtil.isEmptyTrimmed(str));

		// 2. string is empty string
		str = "    ";
		assertTrue(EgovStringUtil.isEmptyTrimmed(str));

		// 3. string is not empty string
		str = "not empty";
		assertFalse(EgovStringUtil.isEmptyTrimmed(str));
	}

	/**
	 * @throws Exception
	 */
	public void testGetTokens() throws Exception {
		// 1. original string
		String str = "a,b,c,d";

		// 2. get token list
		assertEquals(4, EgovStringUtil.getTokens(str).size());
	}


}
