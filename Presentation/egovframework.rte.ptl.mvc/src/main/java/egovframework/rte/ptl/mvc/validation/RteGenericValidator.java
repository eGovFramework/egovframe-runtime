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
package egovframework.rte.ptl.mvc.validation;

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
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30	함철		 최초 생성
 *   2011.09.23	이기하	 isEnglish 메소드 추가
 *   2013.03.22	한성곤	 패스워드 관련 점검 메소드 추가
 *   2017.02.28 장동한	 시큐어코딩(ES)-오류 메시지를 통한 정보노출[CWE-209]
 *
 * </pre>
 */
public final class RteGenericValidator implements Serializable {

	/**
	 *  serialVersion UID
	 */
	private static final long serialVersionUID = -7083990054872323995L;
	
	
    private static final Logger LOGGER = LoggerFactory.getLogger(RteGenericValidator.class);

	private RteGenericValidator() {
		//not called
	}
	
	/**
	 * 주민등록번호 유효성 체크
	 *
	 * @param value
	 * @return
	 */
	public static boolean isValidIdIhNum(String value) {

		//값의 길이가 13자리이며, 7번째 자리가 1,2,3,4 중에 하나인지 check.
		String regex = "\\d{6}[1234]\\d{6}";
		if (!value.matches(regex)) {
			return false;
		}

		//앞 6자리의 값이 유효한 날짜인지 check.
		try {

			String strDate = value.substring(0, 6);
			strDate = ((value.charAt(6) == '1' || value.charAt(6) == '2') ? "19" : "20") + strDate;
			strDate = strDate.substring(0, 4) + "/" + strDate.substring(4, 6) + "/" + strDate.substring(6, 8);

			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
			Date date = dateformat.parse(strDate);
			String resultStr = dateformat.format(date);

			if (!resultStr.equals(strDate)) {
				return false;
			}

		} catch (ParseException e) {
			//2017.02.28 장동한 시큐어코딩(ES)-부적절한 자원 해제[CWE-404]
			//e.printStackTrace();
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
		//모든 태그 "<[^<|>]*>"
		//스크립트 "<script[^>]*>(.*?)</SCRIPT>"
		//스타일 "<STYLE[^>]*>(.*?)</STYLE>"

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
	public static boolean checkCharacterType(String password) {
		char[] charArray = password.toCharArray();

		for (int i = 0; i < charArray.length; i++) {
			char ch = charArray[i];
			/*
			// javascript 부분에는 33~47로 되어 있으나 공백을 처리하기 위해 32~47로 변경함..
			if ((ch >= 32 && ch <= 47) || (ch >= 58 && ch <= 64) || (ch >= 91 && ch <= 96) || (ch >= 123 && ch <= 126)) {
			return false;
			}
			*/
			if (ch < 33 || ch > 126) { // 대부분의 문자를 사용하도록 변경 (javascript도 변경)
				return false;
			}
		}

		return true;
	}

	/**
	 *  패스워드 점검 : 연속된 문자나 순차적인 문자 4개이상 사용금지
	 *
	 * @param password
	 * @return
	 */
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
}
