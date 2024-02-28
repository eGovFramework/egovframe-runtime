/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.fdl.string;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 문자열을 처리하는 유틸 클래스
 * 
 * <p><b>NOTE:</b> Apache Commons regexp 오픈소스를 활용하여 String 관련 기능을 제공하는 유틸이다.</p>
 * @author 실행환경 개발팀 윤성종
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	윤성종			최초 생성
 * 2017.02.15	장동한			시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
 * 2017.02.28	장동한			시큐어코딩(ES)-오류 메시지를 통한 정보노출[CWE-209]
 * </pre>
 */
public final class EgovStringUtil {

    /**
     * <p>
     * 에러나 이벤트와 관련된 각종 메시지를 로깅하기 위한 Log 오브젝트
     * </p>
     */
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovStringUtil.class);

    private static final char WHITE_SPACE = ' ';

    private EgovStringUtil() {
    }

    /**
     * isNull
     */
	public static boolean isNull(String str) {
		if (str != null) {
			str = str.trim();
		}
		return (str == null || str.isEmpty());
	}

    /**
     * isApha
     */
	public static boolean isAlpha(String str) {
		if (str == null) {
			return false;
		}

		int size = str.length();
		if (size == 0) {
			return false;
		}

		for (int i = 0; i < size; i++) {
			if (!Character.isLetter(str.charAt(i))) {
				return false;
			}
		}

		return true;
	}

    /**
     * isAlphaNumeric
     */
	public static boolean isAlphaNumeric(String str) {
		if (str == null) {
			return false;
		}

		int size = str.length();
		if (size == 0) {
			return false;
		}

		for (int i = 0; i < size; i++) {
			if (!Character.isLetterOrDigit(str.charAt(i))) {
				return false;
			}
		}

		return true;
	}

    /**
     * integer2string
     */
    public static String integer2string(int integer) {
        return ("" + integer);
    }

    /**
     * long2string
     */
    public static String long2string(long longdata) {
        return String.valueOf(longdata);
    }

    /**
     * float2string
     */
    public static String float2string(float floatdata) {
        return String.valueOf(floatdata);
    }

    /**
     * double2string
     */
    public static String double2string(double doubledata) {
        return String.valueOf(doubledata);
    }

    /**
     * null2void
     */
	public static String null2void(String str) {
		if (isNull(str)) {
			str = "";
		}
		return str;
	}

    /**
     * string2integer
     */
	public static int string2integer(String str) {
		if (isNull(str)) {
			return 0;
		}
		return Integer.parseInt(str);
	}

    /**
     * string2float
     */
	public static float string2float(String str) {
		if (isNull(str)) {
			return 0.0F;
		}
		return Float.parseFloat(str);
	}

    /**
     * string2float
     */
	public static double string2double(String str) {
		if (isNull(str)) {
			return 0.0D;
		}
		return Double.parseDouble(str);
	}

    /**
     * string2long
     */
	public static long string2long(String str) {
		if (isNull(str)) {
			return 0L;
		}
		return Long.parseLong(str);
	}

    /**
     * null2string
     */
	public static String null2string(String str, String defaultValue) {
		if (isNull(str)) {
			return defaultValue;
		}
		return str;
	}

    /**
     * string2integer
     */
	public static int string2integer(String str, int defaultValue) {
		if (isNull(str)) {
			return defaultValue;
		}
		return Integer.parseInt(str);
	}

    /**
     * string2float
     */
	public static float string2float(String str, float defaultValue) {
		if (isNull(str)) {
			return defaultValue;
		}
		return Float.parseFloat(str);
	}

    /**
     * string2double
     */
	public static double string2double(String str, double defaultValue) {
		if (isNull(str)) {
			return defaultValue;
		}
		return Double.parseDouble(str);
	}

    /**
     * string2long
     */
	public static long string2long(String str, long defaultValue) {
		if (isNull(str)) {
			return defaultValue;
		}
		return Long.parseLong(str);
	}

    /**
     * equals
     */
	public static boolean equals(String source, String target) {
		return null2void(source).equals(null2void(target));
	}

    /**
     * toSubString
     */
	public static String toSubString(String str, int beginIndex, int endIndex) {
		if (equals(str, "")) {
			return str;
		} else if (str.length() < beginIndex) {
			return "";
		} else if (str.length() < endIndex) {
			return str.substring(beginIndex);
		} else {
			return str.substring(beginIndex, endIndex);
		}
	}

    /**
     * toSubString
     */
	public static String toSubString(String source, int beginIndex) {
		if (equals(source, "")) {
			return source;
		} else if (source.length() < beginIndex) {
			return "";
		} else {
			return source.substring(beginIndex);
		}
	}

    /**
     * search
     */
	public static int search(String source, String target) {
		int result = 0;
		String strCheck = source;
		for (int i = 0; i < source.length();) {
			int loc = strCheck.indexOf(target);
			if (loc == -1) {
				break;
			} else {
				result++;
				i = loc + target.length();
				strCheck = strCheck.substring(i);
			}
		}
		return result;
	}

    /**
     * trim
     */
	public static String trim(String str) {
		return str.trim();
	}

    /**
     * Left trim
     */
	public static String ltrim(String str) {
		int i = 0;
		while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
			i++;
		}
		return str.substring(i);
	}

	/**
	 * Right trim
	 */
	public static String rtrim(String str) {
		int i = str.length()-1;
		while (i >= 0 && Character.isWhitespace(str.charAt(i))) {
			i--;
		}
		return str.substring(0, i+1);
	}

	/**
	 * concat
	 */
	public static String concat(String str1, String str2) {
		StringBuilder result = new StringBuilder(str1);
		result.append(str2);
		return result.toString();
	}

	/**
	 * Left pad
	 */
	public static String lPad(String str, int len, char pad) {
		return lPad(str, len, pad, false);
	}

	/**
	 * Left pad
	 */
	public static String lPad(String str, int len, char pad, boolean isTrim) {
		if (isNull(str)) return null;
		if (isTrim) str = trim(str);
        StringBuilder result = new StringBuilder(str);
        for (int i = result.length(); i < len; i++) {
			result.insert(0, pad);
		}
        return result.toString();
	}

	/**
	 * Right pad
	 */
	public static String rPad(String str, int len, char pad) {
		return rPad(str, len, pad, false);
	}

	/**
	 * Right pad
	 */
	public static String rPad(String str, int len, char pad, boolean isTrim) {
		if (isNull(str)) return null;
		if (isTrim) str = trim(str);
        StringBuilder result = new StringBuilder(str);
        for (int i = result.length(); i < len; i++) {
			result.append(pad);
		}
		return result.toString();
	}

	/**
	 * alignLeft
	 */
	public static String alignLeft(String str, int length) {
		return alignLeft(str, length, false);
	}

    /**
     * 문자열의 뒷쪽에 지정한 길이만큼 공백으로 채움
     */
    public static String alignLeft(String str, int length, boolean isEllipsis) {
		StringBuilder result = new StringBuilder(str);
		if (str.length() <= length) {
            for (int i = 0; i < (length - str.length()); i++) {
				result.append(WHITE_SPACE);
            }
            return result.toString();
        } else {
            if (isEllipsis) {
				result.append(str, 0, length - 3);
				result.append("...");
                return result.toString();
            } else {
                return str.substring(0, length);
            }
        }
    }

    /**
     * alignRight
     */
    public static String alignRight(String str, int length) {
        return alignRight(str, length, false);
    }

    /**
     * alignRight
     */
    public static String alignRight(String str, int length, boolean isEllipsis) {
		StringBuilder result = new StringBuilder(length);
		if (str.length() <= length) {
            for (int i = 0; i < (length - str.length()); i++) {
				result.append(WHITE_SPACE);
            }
			result.append(str);
            return result.toString();
        } else {
            if (isEllipsis) {
				result.append(str, 0, length - 3);
				result.append("...");
                return result.toString();
            } else {
                return str.substring(0, length);
            }
        }
    }

    /**
     * alignCenter
     */
    public static String alignCenter(String str, int length) {
        return alignCenter(str, length, false);
    }

    /**
     * alignCenter
     */
    public static String alignCenter(String str, int length, boolean isEllipsis) {
		StringBuilder result = new StringBuilder(length);
		if (str.length() <= length) {
            int leftMargin = (length - str.length()) / 2;
            int rightMargin;
            if ((leftMargin * 2) == (length - str.length())) {
                rightMargin = leftMargin;
            } else {
                rightMargin = leftMargin + 1;
            }
            for (int i = 0; i < leftMargin; i++) {
				result.append(WHITE_SPACE);
            }
			result.append(str);
            for (int i = 0; i < rightMargin; i++) {
				result.append(WHITE_SPACE);
            }
            return result.toString();
        } else {
            if (isEllipsis) {
				result.append(str, 0, length - 3);
				result.append("...");
                return result.toString();
            } else {
                return str.substring(0, length);
            }
        }
    }

    /**
     * capitalize
     */
    public static String capitalize(String str) {
        return !isNull(str) ? str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase() : str;
    }

    /**
     * isPatternMatch
     */
	public static boolean isPatternMatch(String str, String pattern) {
		Matcher matcher = Pattern.compile(pattern).matcher(str);
		LOGGER.debug("{}", matcher);
		return matcher.matches();
	}

	/**
	 * toEng
     */
    public static String toEng(String kor) throws UnsupportedEncodingException {
        if (isNull(kor)) {
            return null;
        }
        return new String(kor.getBytes("KSC5601"), StandardCharsets.ISO_8859_1);
    }

    /**
     * toKor
     */
    public static String toKor(String en) throws UnsupportedEncodingException {
        if (isNull(en)) {
            return null;
        }
        return new String(en.getBytes(StandardCharsets.ISO_8859_1), "euc-kr");
    }

    /**
     * countOf
     */
    public static int countOf(String str, String charToFind) {
        int findLength = charToFind.length();
        int count = 0;
		for (int idx = str.indexOf(charToFind); idx >= 0; idx = str.indexOf(charToFind, idx + findLength)) {
			count++;
        }
        return count;
    }

    /**
     * Encode a string using algorithm specified in web.xml and return the resulting encrypted password.
     * If exception, the plain credentials string is returned
     * @param password Password or other credentials to use in authenticating this username
     * @param algorithm Algorithm used to do the digest
     * @return encypted password based on the algorithm.
     */
	public static String encodePassword(String password, String algorithm) {
		byte[] unencodedPassword = password.getBytes();
		MessageDigest md;

		try {
			md = MessageDigest.getInstance(algorithm);
		//2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("["+e.getClass()+"] Try/Catch... Runing : " + e.getMessage());
			return password;
		}

		md.reset();
		md.update(unencodedPassword);
		byte[] encodedPassword = md.digest();
		StringBuilder result = new StringBuilder();
        for (byte b : encodedPassword) {
            if (((int) b & 0xff) < 0x10) {
				result.append("0");
            }
			result.append(Long.toString((int) b & 0xff, 16));
        }

		return result.toString();
	}

    /**
     * Encode a string using Base64 encoding. Used when storing passwords as cookies.
     * This is weak encoding in that anyone can use the decodeString routine to reverse the encoding.
     * @deprecated use other Base64 encoding utility class
     * @param str String to be encoded
     * @return String encoding result
     */
	@SuppressWarnings("restriction")
	@Deprecated
	public static String encodeString(String str) {
		byte[] encodedBytes = Base64.encodeBase64(str.getBytes());
		return new String(encodedBytes).trim();
	}

    /**
     * Decode a string using Base64 encoding.
     * @deprecated use other Base64 encoding utility class
     * @param str String to be decoded
     * @return String decoding String
     */
	@SuppressWarnings("restriction")
	@Deprecated
	public static String decodeString(String str) {
		byte[] decodedBytes = Base64.decodeBase64(str);
		return new String(decodedBytes);
	}

    /**
     * convert first letter to a big letter or a small letter.<br>
     * <pre>
     * StringUtil.trim('Password') = 'password'
     * StringUtil.trim('password') = 'Password'
     * </pre>
     * @param str String to be swapped
     * @return String converting result
     */
	public static String swapFirstLetterCase(String str) {
		StringBuilder result = new StringBuilder(str);
		result.deleteCharAt(0);
		if (Character.isLowerCase(str.substring(0, 1).toCharArray()[0])) {
			result.insert(0, str.substring(0, 1).toUpperCase());
		} else {
			result.insert(0, str.substring(0, 1).toLowerCase());
		}
		return result.toString();
	}

    /**
     * If original String has a specific String, remove specific Strings from original String.
     * <pre>
     * StringUtil.trim('pass*word', '*') = 'password'
     * </pre>
     * @param origString original String
     * @param trimString String to be trimmed
     * @return converting result
     */
	public static String trim(String origString, String trimString) {
		int startPosit = origString.indexOf(trimString);
		if (startPosit != -1) {
			int endPosit = trimString.length() + startPosit;
			return origString.substring(0, startPosit) + origString.substring(endPosit);
		}
		return origString;
	}

    /**
     * Break a string into specific tokens and return a String of last location.
     * <pre>
     * StringUtil.getLastString('password*password*a*b*c', '*') = 'c'
     * </pre>
     * @param origStr original String
     * @param strToken specific tokens
     * @return String of last location
     */
	public static String getLastString(String origStr, String strToken) {
		StringTokenizer str = new StringTokenizer(origStr, strToken);
		String lastStr = "";
		while (str.hasMoreTokens()) {
			lastStr = str.nextToken();
		}
		return lastStr;
	}

    /**
     * If original String has token, Break a string into specific tokens and change String Array.
     * If not, return a String Array which has original String as it is.
     * <pre>
     * StringUtil.getStringArray('passwordabcpassword', 'abc') = String[]{'password','password'}
     * StringUtil.getStringArray('pasword*password', 'abc') = String[]{'pasword*password'}
     * </pre>
     * @param str original String
     * @param strToken specific String token
     * @return String[]
     */
	public static String[] getStringArray(String str, String strToken) {
		if (str.contains(strToken)) {
			StringTokenizer st = new StringTokenizer(str, strToken);
			String[] stringArray = new String[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++) {
				stringArray[i] = st.nextToken();
			}
			return stringArray;
		}
		return new String[] { str };
	}

    /**
     * If string is null or empty string, return false. <br>
     * If not, return true.
     *
     * <pre>
     * StringUtil.isNotEmpty('') = false
     * StringUtil.isNotEmpty(null) = false
     * StringUtil.isNotEmpty('abc') = true
     * </pre>
     * @param str original String
     * @return which empty string or not.
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * If string is null or empty string, return true. <br>
     * If not, return false.
     *
     * <pre>
     * StringUtil.isEmpty('') = true
     * StringUtil.isEmpty(null) = true
     * StringUtil.isEmpty('abc') = false
     * </pre>
     * @param str original String
     * @return which empty string or not.
     */
	public static boolean isEmpty(String str) {
		return (str == null || str.isEmpty());
	}

    /**
     * replace replaced string to specific string from original string. <br>
     *
     * <pre>
     * StringUtil.replace('work$id', '$', '.') 	= 'work.id'
     * </pre>
     * @param str original String
     * @param replacedStr to be replaced String
     * @param replaceStr replace String
     * @return converting result
     */
	public static String replace(String str, String replacedStr, String replaceStr) {
		String newStr = "";
		if (str.contains(replacedStr)) {
			String s1 = str.substring(0, str.indexOf(replacedStr));
			String s2 = str.substring(str.indexOf(replacedStr) + 1);
			newStr = s1 + replaceStr + s2;
		}
		return newStr;
	}

	/**
     *  It returns true if str matches the pattern string. It performs regular expression pattern matching.
     *
     * <pre>
     * StringUtil.isPatternMatching('abc-def', '*-*') 	= true
     * StringUtil.isPatternMatching('abc', '*-*') 	= false
     * </pre>
     * @param str original String
     * @param pattern pattern String
     * @return boolean which matches the pattern string or not.
     * @throws Exception fail to check pattern matched
     */
	public static boolean isPatternMatching(String str, String pattern) throws Exception {
		if (pattern.indexOf('*') >= 0) {
			pattern = pattern.replaceAll("\\*", ".*");
		}
		pattern = "^" + pattern + "$";
		return Pattern.matches(pattern, str);
	}

    /**
     * It returns true if string contains a sequence of the same character.
     *
     * <pre>
     * StringUtil.containsMaxSequence('password', '2') 	= true
     * StringUtil.containsMaxSequence('my000', '3') 	= true
     * StringUtil.containsMaxSequence('abbbbc', '5')	= false
     * </pre>
     * @param str original String
     * @param maxSeqNumber a sequence of the same character
     * @return which contains a sequence of the same character
     */
	public static boolean containsMaxSequence(String str, String maxSeqNumber) {
		int occurence = 1;
		int max = string2integer(maxSeqNumber);
		if (str == null) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < (sz - 1); i++) {
			if (str.charAt(i) == str.charAt(i + 1)) {
				occurence++;
				if (occurence == max) {
					return true;
				}
			} else {
				occurence = 1;
			}
		}
		return false;
	}

    /**
     * <p>
     * Checks that the String contains certain  characters.
     * </p>
     * <p>
     * A <code>null</code> String will return <code>false</code>. A <code>null</code> invalid character array will return <code>false</code>.
     * An empty String ("") always returns false.
     * </p>
     *
     * <pre>
     * StringUtil.containsInvalidChars(null, *) = false
     * StringUtil.containsInvalidChars(*, null) = false
     * StringUtil.containsInvalidChars(&quot;&quot;, *) = false
     * StringUtil.containsInvalidChars(&quot;ab&quot;, '') = false
     * StringUtil.containsInvalidChars(&quot;abab&quot;, 'xyz') = false
     * StringUtil.containsInvalidChars(&quot;ab1&quot;, 'xyz') = false
     * StringUtil.containsInvalidChars(&quot;xbz&quot;, 'xyz') = true
     * </pre>
     * @param str the String to check, may be null
     * @param invalidChars an array of invalid chars, may be null
     * @return false if it contains none of the invalid chars, or is null
     */
	public static boolean containsInvalidChars(String str, char[] invalidChars) {
		if (str == null || invalidChars == null) {
			return false;
		}
		int strSize = str.length();
		for (int i = 0; i < strSize; i++) {
			char ch = str.charAt(i);
            for (char invalidChar : invalidChars) {
                if (invalidChar == ch) {
                    return true;
                }
            }
		}
		return false;
	}

    /**
     * <p>
     * Checks that the String contains certain characters.
     * </p>
     * <p>
     * A <code>null</code> String will return <code>false</code>. A <code>null</code> invalid character array will return <code>false</code>.
     * An empty String ("") always returns false.
     * </p>
     *
     * <pre>
     * StringUtil.containsInvalidChars(null, *) = false
     * StringUtil.containsInvalidChars(*, null) = false
     * StringUtil.containsInvalidChars(&quot;&quot;, *) = false
     * StringUtil.containsInvalidChars(&quot;ab&quot;, '')  = false
     * StringUtil.containsInvalidChars(&quot;abab&quot;, 'xyz') = false
     * StringUtil.containsInvalidChars(&quot;ab1&quot;, 'xyz') = false
     * StringUtil.containsInvalidChars(&quot;xbz&quot;, 'xyz') = true
     * </pre>
     * @param str the String to check, may be null
     * @param invalidChars a String of invalid chars, may be null
     * @return false if it contains none of the invalid chars, or is null
     */
	public static boolean containsInvalidChars(String str, String invalidChars) {
		if (str == null || invalidChars == null) {
			return true;
		}
		return containsInvalidChars(str, invalidChars.toCharArray());
	}

     /**
     * <p>
     * Checks if the String contains only unicode digits. A decimal point is not a unicode digit and returns false.
     * </p>
     * <p>
     *  <code>null</code> will return <code>false</code>. An empty String ("") will return <code>false</code>.
     * </p>
     *
     * <pre>
     * StringUtil.isNumeric(null) = false
     * StringUtil.isNumeric(&quot;&quot;) = false
     * StringUtil.isNumeric(&quot;  &quot;) = false
     * StringUtil.isNumeric(&quot;123&quot;)  = true
     * StringUtil.isNumeric(&quot;12 3&quot;) = false
     * StringUtil.isNumeric(&quot;ab2c&quot;) = false
     * StringUtil.isNumeric(&quot;12-3&quot;) = false
     * StringUtil.isNumeric(&quot;12.3&quot;) = false
     * </pre>
     * @param str the String to check, may be null
     * @return <code>true</code> if only contains digits, and is non-null
     */
	public static boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		if (sz == 0) {
			return false;
		}
		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

    /**
     * <p>
     * Reverses a String as per
     * {@link StringBuffer#reverse()}.
     * </p>
     * <p>
     * <code>null</code> String returns <code>null</code>.
     * </p>
     *
     * <pre>
     * StringUtil.reverse(null) = null
     * StringUtil.reverse(&quot;&quot;) = &quot;&quot;
     * StringUtil.reverse(&quot;bat&quot;) = &quot;tab&quot;
     * </pre>
     * @param str the String to reverse, may be null
     * @return the reversed String, <code>null</code> if null String input
     */
	public static String reverse(String str) {
		if (str == null) {
			return null;
		}
		return new StringBuffer(str).reverse().toString();
	}

    /**
     * Make a new String that filled original to a special char as cipers
     * @param originalStr original String
     * @param ch a special char
     * @param cipers cipers
     * @return filled String
     */
	public static String fillString(String originalStr, char ch, int cipers) {
		int originalStrLength = originalStr.length();
		if (cipers < originalStrLength) {
			return null;
		}
		int difference = cipers - originalStrLength;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < difference; i++) {
			result.append(ch);
		}
		result.append(originalStr);
		return result.toString();
	}

    /**
     * Determine whether a (trimmed) string is empty
     * @param foo The text to check.
     * @return Whether empty.
     */
	public static boolean isEmptyTrimmed(String foo) {
		return (foo == null || foo.trim().isEmpty());
	}

    /**
     * Return token list
     * @param lst using commas as separators
     * @param separator separators
	 * @return List result
     */
	public static List<String> getTokens(String lst, String separator) {
		List<String> tokens = new ArrayList<String>();
		if (lst != null) {
			StringTokenizer st = new StringTokenizer(lst, separator);
			while (st.hasMoreTokens()) {
				try {
					String en = st.nextToken().trim();
					tokens.add(en);
				//2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
				} catch(IllegalArgumentException e) {
					LOGGER.error("[IllegalArgumentException] Try/Catch...usingParameters Operation Runing : "+ e.getMessage());
				}
			}
		}
		return tokens;
	}

    /**
     * Return token list which is separated by ","
	 * @param lst using commas as separators
     * @return List result
     */
	public static List<?> getTokens(String lst) {
        return getTokens(lst, ",");
    }

    /**
     * This method convert "string_util" to "stringUtil"
     * @param targetString targetString
     * @param posChar posChar
     * @return String result
     */
	public static String convertToCamelCase(String targetString, char posChar) {
		StringBuilder result = new StringBuilder();
		boolean nextUpper = false;
		String allLower = targetString.toLowerCase();
		for (int i = 0; i < allLower.length(); i++) {
			char currentChar = allLower.charAt(i);
			if (currentChar == posChar) {
				nextUpper = true;
			} else {
				if (nextUpper) {
					currentChar = Character.toUpperCase(currentChar);
					nextUpper = false;
				}
				result.append(currentChar);
			}
		}
		return result.toString();
	}

    /**
     * Convert a string that may contain underscores to camel case.
     * @param underScore Underscore name.
     * @return Camel case representation of the underscore string.
     */
    public static String convertToCamelCase(String underScore) {
        return convertToCamelCase(underScore, '_');
    }

    /**
     * Convert a camel case string to underscore representation.
     * @param camelCase Camel case name.
     * @return Underscore representation of the camel case string.
     */
	public static String convertToUnderScore(String camelCase) {
		String result = "";
		for (int i = 0; i < camelCase.length(); i++) {
			char currentChar = camelCase.charAt(i);
			if (i > 0 && Character.isUpperCase(currentChar)) {
				result = result.concat("_");
			}
			result = result.concat(Character.toString(currentChar).toLowerCase());
		}
		return result;
	}

}
