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
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	함철				최초 생성
 * 2011.09.23	이기하			validateEnglish 메소드 추가
 * 2013.03.22	한성곤			패스워드 관련 점검 메소드 추가
 * 2020.07.22	윤주호			패스워드 점검 강화 관련 메소드 수정
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
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	@Deprecated
	public static boolean validatePassword2(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);
		if (!RteGenericValidator.checkCharacterType(password)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}
		return true;
	}

	/**
	 * 패스워드 점검 : 연속된 문자나 숫자인 문자 4개이상 사용금지
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	@Deprecated
	public static boolean validatePassword3(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);
		if (!RteGenericValidator.checkSeries(password)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}
		return true;
	}

	/**
	 * 패스워드 점검 : 연속된 문자나 숫자인 문자 4개이상 사용금지
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	@Deprecated
	public static boolean validatePassword4(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);
		if (!RteGenericValidator.checkSeries(password)) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}
		return true;
	}
	
	/**
	 * 패스워드 점검 : [조합] 영문자,숫자,특수문자(~!@#$%^&*?)의 최소 3가지 조합
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validatePwdCheckComb3(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);
		if ( !RteGenericValidator.isMoreThan2CharTypeComb(password) ) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}
		return true;
	}

	/**
	 * 패스워드 점검 : [조합] 영대문자,영소문자,숫자,특수문자(~!@#$%^&*?)의 최소 4가지 조합
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validatePwdCheckComb4(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);
		if ( !RteGenericValidator.isMoreThan3CharTypeComb(password) ) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}
		return true;
	}
	
	/**
	 * 패스워드 점검 : [연속] 연속된 3개 이상의 문자나 숫자 사용 금지
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validatePwdCheckSeries(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);
		if ( RteGenericValidator.isSeriesCharacter(password) ) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}
		return true;
	}
	
	/**
	 * 패스워드 점검 : [반복] 반복된 3개 이상의 문자나 숫자 사용 금지
	 * </pre>
	 *
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @return
	 */
	public static boolean validatePwdCheckRepeat(Object bean, ValidatorAction va, Field field, Errors errors) {
		String password = FieldChecks.extractValue(bean, field);
		if ( RteGenericValidator.isRepeatCharacter(password) ) {
			FieldChecks.rejectValue(errors, field, va);
			return false;
		}
		return true;
	}

}
