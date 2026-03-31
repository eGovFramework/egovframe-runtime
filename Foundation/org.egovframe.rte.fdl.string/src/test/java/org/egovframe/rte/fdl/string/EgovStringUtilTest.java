package org.egovframe.rte.fdl.string;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class EgovStringUtilTest {

    /**
     * String이 특정 Pattern(정규표현식)에 부합하는지 검사한다.
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
     */
    @Test
    public void testToSubString() {
        String source = "substring test";
        assertEquals("test", EgovStringUtil.toSubString(source, 10));
        assertEquals("string", EgovStringUtil.toSubString(source, 3, 9));
    }

    /**
     * 전체 String 중 앞뒤에 존재하는 공백 문자(white character)를 제거한다.
     */
    @Test
    public void testStringTrim() {
        String str = "  substring  ";
        assertEquals("substring", EgovStringUtil.trim(str));
        assertEquals("substring  ", EgovStringUtil.ltrim(str));
        assertEquals("  substring", EgovStringUtil.rtrim(str));
    }

    /**
     * 두 String을 붙여서 하나의 String을 생성한다.
     */
    @Test
    public void testConcatenate() {
        String str1 = "substring";
        String str2 = "test";
        assertEquals("substringtest", EgovStringUtil.concat(str1, str2));
    }

    /**
     * 전체 String 중 특정 String Pattern이 있는지 찾는다.
     */
    @Test
    public void testFindPattern() {
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
     */
    @Test
    public void testIsNull() {
        // 1. check empty string
        assertTrue(EgovStringUtil.isNull(""));
        // 2. check null
        assertTrue(EgovStringUtil.isNull(null));
        // 3. check not null
        assertFalse(EgovStringUtil.isNull("string"));
    }

    /**
     * string이 알파벳인지 검사한다.
     */
    @Test
    public void testIsAlpha() {
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
     *
     * @throws Exception
     */
    @Test
    public void testIsAlphaNumeric() {
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

    @Test
    public void testNull2void() {
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
     */
    @Test
    public void testTypeConversion() {
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

    @Test
    public void testLPad() {
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

    @Test
    public void testAlignString() {
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

    @Test
    public void testEncodePassword() {
        // 1. try to encode password and compare
        String encoded1 = EgovStringUtil.encodePassword("password", "MD5");
        String encoded2 = EgovStringUtil.encodePassword("password", "MD5");
        assertEquals(encoded1, encoded2);
        // 2. define not available algorithm 'MD6 MessageDigest'
        String encoded3 = EgovStringUtil.encodePassword("password", "MD6");
        assertEquals("password", encoded3);
    }

    @Test
    public void testSwapFirstLetterCase() {
        // 1. In case, first letter is small letter. try to swap.
        String swapped = EgovStringUtil.swapFirstLetterCase("password");
        assertEquals("Password", swapped);
        // 2. In case, first letter is big letter. try to swap.
        swapped = EgovStringUtil.swapFirstLetterCase("PASSWORD");
        assertEquals("pASSWORD", swapped);
    }

    @Test
    public void testTrim() {
        // 1. try to trim when trimmed string is 'trim'
        String trimmed = EgovStringUtil.trim("passwordtrimpassword", "trim");
        assertEquals("passwordpassword", trimmed);
        // 2. try to trim when trimmed string is ','
        trimmed = EgovStringUtil.trim("passwordtrimpassword", ",");
        assertEquals("passwordtrimpassword", trimmed);
    }

    @Test
    public void testGetLastString() {
        // 1. get last string when token is ','
        String trimmed = EgovStringUtil.getLastString("password,password", ",");
        assertEquals("password", trimmed);
        // 2. get last string when original doesn't have token.
        trimmed = EgovStringUtil.getLastString("password,password", "*");
        assertEquals("password,password", trimmed);
    }

    @Test
    public void testGetStringArray() {
        // 1. when original string has token, get string array.
        String[] strings = EgovStringUtil.getStringArray("password,password", ",");
        assertEquals(2, strings.length);
        // 2. when original string doesn't have token, get string array.
        strings = EgovStringUtil.getStringArray("password", ",");
        assertEquals(1, strings.length);
    }

    @Test
    public void testIsNotEmpty() {
        // 1. check which string is not empty or not
        assertTrue(EgovStringUtil.isNotEmpty("passwordtrimpassword"));
    }

    @Test
    public void testIsEmpty() {
        // 1. check empty string
        assertTrue(EgovStringUtil.isEmpty(""));
        // 2. check null
        assertTrue(EgovStringUtil.isEmpty(null));
    }

    @Test
    public void testReplace() {
        String replaced = EgovStringUtil.replace("password,password", ",", "-");

        assertEquals("password-password", replaced);
    }

    @Test
    public void testContainsMaxSequence() {
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

    @Test
    public void testContainsInvalidChars() {
        // 1. string is empty.
        String str = "";
        char[] invalidChars = new char[]{'*', '%'};
        assertTrue(!EgovStringUtil.containsInvalidChars(str, invalidChars));
        // 2. string is null.
        str = null;
        assertTrue(!EgovStringUtil.containsInvalidChars(str, invalidChars));
        // 3. invalid chars doesn't defined.
        str = "";
        assertTrue(!EgovStringUtil.containsInvalidChars(str, new char[]{}));
        // 4. string has invalid chars.
        str = "x*yz";
        assertTrue(EgovStringUtil.containsInvalidChars(str, invalidChars));
        assertTrue(EgovStringUtil.containsInvalidChars(str, "yz"));
    }

    @Test
    public void testIsNumeric() {
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

    @Test
    public void testReverse() {
        // 1. string is null
        String str = null;
        assertNull(EgovStringUtil.reverse(str));
        // 2. string is 'bat'
        str = "bat";
        assertEquals("tab", EgovStringUtil.reverse(str));
    }

    @Test
    public void testFillString() {
        String originalStr = "1";
        char ch = '0';
        int cipers = 6;
        assertEquals("000001", EgovStringUtil.fillString(originalStr, ch, cipers));
        originalStr = "12345";
        cipers = 4;
        assertNull(EgovStringUtil.fillString(originalStr, ch, cipers));
    }

    @Test
    public void testIsEmptyTrimmed() {
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

    @Test
    public void testGetTokens() {
        // 1. original string
        String str = "a,b,c,d";
        // 2. get token list
        assertEquals(4, EgovStringUtil.getTokens(str).size());
    }

}
