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

import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.springframework.validation.Errors;
import org.springmodules.validation.commons.FieldChecks;

/**
 * RteFieldChecks.java
 * <p/><b>NOTE:</b>
 * <pre> 주민등록번호, 한글체크 같은 Jakarta Commons Validator에서 제공되지 않는 validation rule을 추가로 제공한다.
 * 실제 validation check는 RteGenericValidator에 위임한다.</pre>
 *
 * @author 실행환경 개발팀 함철
 * @since 2009.06.01
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일            수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30	함철		최초 생성
 *   2011.09.23	이기하	validateEnglish 메소드 추가
 *   2013.03.22	한성곤	패스워드 관련 점검 메소드 추가
 *
 * </pre>
 */
public class RteFieldChecks extends FieldChecks {

	/**
	 *  serialVersion UID
	 */
	private static final long serialVersionUID = 611324405170619860L;

	/**
	 * 주민등록번호 유효성 체크
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validateIhIdNum(Object bean, ValidatorAction va, Field field, Errors errors) {

		String ihidnum = FieldChecks.extractValue(bean, field);

		if (!RteGenericValidator.isValidIdIhNum(ihidnum)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 한글여부 체크
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validateKorean(Object bean, ValidatorAction va, Field field, Errors errors) {

		String value = FieldChecks.extractValue(bean, field);

		if (!RteGenericValidator.isKorean(value)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 영어여부 체크
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validateEnglish(Object bean, ValidatorAction va, Field field, Errors errors) {

		String value = FieldChecks.extractValue(bean, field);

		if (!RteGenericValidator.isEnglish(value)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * HTML tag 포함여부 체크
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validateHtmlTag(Object bean, ValidatorAction va, Field field, Errors errors) {

		String value = FieldChecks.extractValue(bean, field);

		if (!RteGenericValidator.isHtmlTag(value)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 패스워드 점검 : 8~20자 이내
	 *
	 * <p>Rules' javascript Example:</p>
	 * <pre>
	function validatePassword1(form) {
	var bValid = true;
	var focusField = null;
	var i = 0;
	var fields = new Array();
	oPassword = new password1();
	for (x in oPassword) {
		var field = form[oPassword[x][0]];
		if (field.type == 'password') {
			if (trim(field.value).length == 0 || !checkPassword1(field)) {
				if (i == 0) {
					focusField = field;
				}
				fields[i++] = oPassword[x][1];
				bValid = false;
			}
		}
	}
	if (fields.length > 0) {
		focusField.focus();
		alert(fields.join('\n'));
	}
	return bValid;
	}

	function checkPassword1(pwd) {
	p_pass = pwd.value;

	if (pwd.value.length < 8 || pwd.value.length > 20) {

		pwd.value = "";
		pwd.focus();
		return false;
	}
	return pwd;
	}
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validatePassword1(Object bean, ValidatorAction va, Field field, Errors errors) {

		String password = FieldChecks.extractValue(bean, field);

		if (!RteGenericValidator.checkLength(password)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}

		return true;
	}

	/**
	 * 패스워드 점검 : 한글,특수문자,띄어쓰기는 안됨
	 *
	 * <p>Rules' javascript Example:</p>
	 * <pre>
	function validatePassword2(form) {
	var bValid = true;
	var focusField = null;
	var i = 0;
	var fields = new Array();
	oPassword = new password2();
	for (x in oPassword) {
		var field = form[oPassword[x][0]];
		if (field.type == 'password') {
			if (trim(field.value).length == 0 || !checkPassword2(field)) {
				if (i == 0) {
					focusField = field;
				}
				fields[i++] = oPassword[x][1];
				bValid = false;
			}
		}
	}
	if (fields.length > 0) {
		focusField.focus();
		alert(fields.join('\n'));
	}
	return bValid;
	}

	function checkPassword2(pwd) {
	var str = pwd.value;
	for ( var i = 0; i < str.length; i++) {
		ch_char = str.charAt(i);
		ch = ch_char.charCodeAt();
		if ((ch >= 33 && ch <= 47) || (ch >= 58 && ch <= 64)
				|| (ch >= 91 && ch <= 96) || (ch >= 123 && ch <= 126)) {
			return false;
		}
	}
	return pwd;
	}
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validatePassword2(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);

		if (!RteGenericValidator.checkCharacterType(password)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}

		return true;
	}

	/**
	 * 패스워드 점검 : 연속된 문자나 순차적인 문자 4개이상 사용금지
	 *
	 * <p>Rules' javascript Example:</p>
	 * <pre>
	function validatePassword3(form) {
	var bValid = true;
	var focusField = null;
	var i = 0;
	var fields = new Array();
	oPassword = new password3();
	for (x in oPassword) {
		var field = form[oPassword[x][0]];
		if (field.type == 'password') {
			if (trim(field.value).length == 0 || !checkPassword3(field)) {
				if (i == 0) {
					focusField = field;
				}
				fields[i++] = oPassword[x][1];
				bValid = false;
			}
		}
	}
	if (fields.length > 0) {
		focusField.focus();
		alert(fields.join('\n'));
	}
	return bValid;
	}

	function checkPassword3(pwd) {
	p_pass = pwd.value;
	var cnt1 = 0, cnt2 = 1, cnt3 = 1;

	for (var i = 0; i < p_pass.length; i++) {
		temp_pass1 = p_pass.charAt(i);
		next_pass = (parseInt(temp_pass1.charCodeAt(0))) + 1;
		temp_p = p_pass.charAt(i + 1);
		temp_pass2 = (parseInt(temp_p.charCodeAt(0)));
		if (temp_pass2 == next_pass)
			cnt2 = cnt2 + 1;
		else
			cnt2 = 1;
		if (temp_pass1 == temp_p)
			cnt3 = cnt3 + 1;
		else
			cnt3 = 1;
		if (cnt2 > 3)
			break;
		if (cnt3 > 3)
			break;
	}
	if (cnt2 > 3) {
		pwd.value = "";
		pwd.focus();
		return false;
	}
	return pwd;
	}
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validatePassword3(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);

		if (!RteGenericValidator.checkSeries(password)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}

		return true;
	}

	/**
	 * 패스워드 점검 : 반복문자나 숫자 연속 4개이상 사용금지
	 *
	 * <p>Rules' javascript Example:</p>
	 * <pre>
	function validatePassword4(form) {
	var bValid = true;
	var focusField = null;
	var i = 0;
	var fields = new Array();
	oPassword = new password4();
	for (x in oPassword) {
		var field = form[oPassword[x][0]];
		if (field.type == 'password') {
			if (trim(field.value).length == 0 || !checkPassword4(field)) {
				if (i == 0) {
					focusField = field;
				}
				fields[i++] = oPassword[x][1];
				bValid = false;
			}
		}
	}
	if (fields.length > 0) {
		focusField.focus();
		alert(fields.join('\n'));
	}
	return bValid;
	}

	function checkPassword4(pwd) {
	p_pass = pwd.value;
	var cnt1 = 0, cnt2 = 1, cnt3 = 1;

	for (var i = 0; i < p_pass.length; i++) {
		temp_pass1 = p_pass.charAt(i);
		next_pass = (parseInt(temp_pass1.charCodeAt(0))) + 1;
		temp_p = p_pass.charAt(i + 1);
		temp_pass2 = (parseInt(temp_p.charCodeAt(0)));
		if (temp_pass2 == next_pass)
			cnt2 = cnt2 + 1;
		else
			cnt2 = 1;
		if (temp_pass1 == temp_p)
			cnt3 = cnt3 + 1;
		else
			cnt3 = 1;
		if (cnt2 > 3)
			break;
		if (cnt3 > 3)
			break;
	}
	if (cnt3 > 3) {
		pwd.value = "";
		pwd.focus();
		return false;
	}
	return pwd;
	}
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validatePassword4(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);

		if (!RteGenericValidator.checkSeries(password)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}

		return true;
	}

}
