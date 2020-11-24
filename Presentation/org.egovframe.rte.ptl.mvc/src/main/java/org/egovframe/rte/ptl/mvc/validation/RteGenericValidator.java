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
package org.egovframe.rte.ptl.mvc.validation;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RteGenericValidator.java
 * <p/><b>NOTE:</b>
 * <pre> 실제적인 validation check 로직을 수행한다.
 * 주민등록번호, 한글체크 같은 Jakarta Commons Validator에서 제공되지 않는 validation rule을 추가로 제공하기 위해 작성되었다.
 * </pre>
 * @author 실행환경 개발팀 함철
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	함철				최초 생성
 * 2011.09.23	이기하				isEnglish 메소드 추가
 * 2013.03.22	한성곤				패스워드 관련 점검 메소드 추가
 * 2017.02.28	장동한				시큐어코딩(ES)-오류 메시지를 통한 정보노출[CWE-209]
 * 2020.07.20	윤주호				패스워드 점검 강화 관련 메소드 수정
 * </pre>
 */
public final class RteGenericValidator implements Serializable {

	private static final long serialVersionUID = -7083990054872323995L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RteGenericValidator.class);

	private RteGenericValidator() {
	}
	
	/**
	 * 주민등록번호 유효성 체크
	 *
	 * @param value
	 * @return
	 */
	public static boolean isValidIdIhNum(String value) {
		String regex = "\\d{13}";
		if (!value.matches(regex)) {
			return false;
		}

		try {
			String strDate = value.substring(0, 6);
			String gubun = String.valueOf(value.charAt(6));
			switch(gubun) {
				case "1":
				case "2":
				case "5":
				case "6":
					strDate = "19" + strDate;
					break;
				case "3":
				case "4":
				case "7":
				case "8":
					strDate = "20" + strDate;
					break;
				case "9":
				case "0":
					strDate = "18" + strDate;
					break;
			}
			strDate = strDate.substring(0, 4) + "/" + strDate.substring(4, 6) + "/" + strDate.substring(6, 8);

			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
			Date date = dateformat.parse(strDate);
			String resultStr = dateformat.format(date);

			if (!resultStr.equals(strDate)) {
				return false;
			}
		} catch (ParseException e) {
			//2017.02.28 장동한 시큐어코딩(ES)-부적절한 자원 해제[CWE-404]
			LOGGER.error("["+e.getClass()+"] Try/Catch...isValidIdIhNum(String value) Runing : " + e.getMessage());
			return false;
		}

		//주민등록번호 마지막 자리를 이용한 check.
		char[] charArray = value.toCharArray();
		long sum = 0;
		int[] arrDivide = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5 };
		for (int i = 0; i < charArray.length - 1; i++) {
			sum += Integer.parseInt(String.valueOf(charArray[i])) * arrDivide[i];
		}

		int checkdigit = (int) ((int) (11 - sum % 11)) % 10;

		return (checkdigit == Integer.parseInt(String.valueOf(charArray[12]))) ? true : false;
	}

	/**
	 * 한글여부 체크
	 *
	 * @param value
	 * @return
	 */
	public static boolean isKorean(String value) {
		char[] charArray = value.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			if (Character.getType(charArray[i]) != 5) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 영어여부 체크
	 *
	 * @param value
	 * @return
	 */
	public static boolean isEnglish(String value) {
		char[] charArray = value.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			if (Character.getType(charArray[i]) != 1 && Character.getType(charArray[i]) != 2) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Html tag 포함 여부
	 *
	 * @param value
	 * @return
	 */
	public static boolean isHtmlTag(String value) {
		Pattern re = Pattern.compile("<[^<|>]*>");
		Matcher m = re.matcher(value);
		if (m.find()) {
			return false;
		}
		return true;
	}

	/**
	 * 패스워드 점검 : 8~20자 이내
	 *
	 * @param password
	 * @return
	 */
	public static boolean checkLength(String password) {
		if (password.length() < 8 || password.length() > 20) {
			return false;
		}
		return true;
	}

	/**
	 * 패스워드 점검 : 한글,특수문자,띄어쓰기는 안됨
	 *
	 * @param password
	 * @return
	 */
	@Deprecated
	public static boolean checkCharacterType(String password) {
		char[] charArray = password.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char ch = charArray[i];
			if (ch < 33 || ch > 126) { // 대부분의 문자를 사용하도록 변경 (javascript도 변경)
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 패스워드 점검 : 최소 3가지 조합. 영문자, 숫자, 특수문자 $@$!%*#?& 필수
	 *
	 * @param password
	 * @return boolean 일치여부
	 */
	public static boolean isMoreThan2CharTypeComb(String password) {
		//영문대소문자 + 숫자 + 특수문자 조합 
		//한글 , 빈칸 제외
		String ALLOWED_SPECIAL_CHAR = "~!@#$%^&*?";
		String REGEXSTR = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*["+ALLOWED_SPECIAL_CHAR+"])[A-Za-z\\d"+ALLOWED_SPECIAL_CHAR+"]+$";

		Pattern re = Pattern.compile(REGEXSTR);
		Matcher m = re.matcher(password);
		return m.find();
	}
	
	/**
	 * 패스워드 점검 : 최소 4가지 조합. 영문 대문자, 영문 소문자, 숫자, 특수문자 ~!@#$%^&*? 필수
	 *
	 * @param password
	 * @return
	 */
	public static boolean isMoreThan3CharTypeComb(String password) {
		//영문대문자+영문소문자 + 숫자 + 특수문자 조합 
		//한글 , 빈칸 제외
		String ALLOWED_SPECIAL_CHAR	= "~!@#$%^&*?";
		String REGEXSTR = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*["+ALLOWED_SPECIAL_CHAR+"])[A-Za-z\\d"+ALLOWED_SPECIAL_CHAR+"]+$";

		Pattern re = Pattern.compile(REGEXSTR);
		Matcher m = re.matcher(password);
		return m.find();
	}

	/**
	 * 패스워드 점검 : 연속된 반복문자
	 *
	 * @param password
	 * @param reapeatedTimes 최대 반복 횟수
	 * @return
	 */
	public static boolean isRepeatedNTimes(String password,int reapeatedTimes) {
		//N번 반복된 문자
		// \1 backReference
		String REGEXSTR	= "(.)\\1{"+reapeatedTimes+",}";

		Pattern re = Pattern.compile(REGEXSTR);
		Matcher m = re.matcher(password);
		return m.find();
	}

	/**
	 *  패스워드 점검 : 연속된 문자나 순차적인 문자 4개이상 사용금지
	 *
	 * @param password
	 * @return
	 */
	@Deprecated
	public static boolean checkSeries(String password) {
		int countSequence = 0, countSame = 0;

		for (int i = 0; i < password.length() - 1; i++) {
			char pass = password.charAt(i);
			char next = (char) (pass + 1);

			if (password.charAt(i + 1) == next) {
				countSequence++;
			} else {
				countSequence = 0;
			}

			if (pass == password.charAt(i + 1)) {
				countSame++;
			} else {
				countSame = 0;
			}

			if (countSequence > 2 || countSame > 2) {
				return false;
			}
		}

		return true;
	}

	/**
	 *  패스워드 점검 : [연속] 연속된 3개 이상의 문자나 숫자 사용 금지
	 *
	 * @param rawPassword
	 * @return
	 */
	public static boolean isSeriesCharacter(String rawPassword) {
		//최대 연속 횟수 설정
		int MAX_SERIES_CNT = 2;
		//감소 Cnt, 증가 Cnt 초기화
		int seriesDecCnt = 1, seriesIncCnt = 1;
		
		for (int i = 0; i < rawPassword.length() - 1; i++) {
			char currentChar = rawPassword.charAt(i);
			char nextChar = rawPassword.charAt(i + 1);

			//연속 판단
			//감소된 CharCode, 증가된 CharCode
			char decreasedCharCode = (char) (currentChar - 1);
			char increasedCharCode = (char) (currentChar + 1);

			if(decreasedCharCode == nextChar) {
				seriesDecCnt++ ; 
			} else { 
				seriesDecCnt = 1;
			}

			if(increasedCharCode == nextChar) {
				seriesIncCnt++ ; 
			} else { 
				seriesIncCnt = 1;
			}

			if (seriesDecCnt > MAX_SERIES_CNT || seriesIncCnt > MAX_SERIES_CNT) {
				return true;
			}
		}

		return false;
	}

	/**
	 *  패스워드 점검 : [반복] 반복된 3개 이상의 문자나 숫자 사용 금지
	 *
	 * @param rawPassword
	 * @return
	 */
	public static boolean isRepeatCharacter(String rawPassword) {
		//최대 반복 가능 횟수 설정
		int MAX_RPEAT_CNT = 2;
		//감소 Cnt, 증가 Cnt 초기화
		int repeatCnt = 1;

		for (int i = 0; i < rawPassword.length() - 1; i++) {
			char currentChar = rawPassword.charAt(i);
			char nextChar = rawPassword.charAt(i + 1);

			//반복 판단
			if(currentChar == nextChar) {
				repeatCnt++ ;
			} else {
				repeatCnt = 1;
			}

			if (repeatCnt > MAX_RPEAT_CNT) {
				return true;
			}
		}

		return false;
	}

}
