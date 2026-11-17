/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.ai.core.pii;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * 주민등록번호(외국인등록번호 포함) 값 규칙 — 색인 마스킹의 오탐 억제용.
 *
 * <p>규칙은 두 단계다. <b>형식 검증</b>은 13자리 숫자·성별/세기 코드(1~8)·실존하는 생년월일을 본다. 2020-10 이후 부여되는
 * 신형 번호는 뒤 7자리가 임의 번호라 검증번호로 판별할 수 없으므로 형식 검증만 통과하면 된다. <b>레거시 검증번호</b>는
 * 형식 검증에 더해 구 체계의 마지막 자리 검증번호(가중치 2~9,2~5 / 외국인은 +2 보정)까지 일치해야 한다.</p>
 *
 * <p>소비처가 색인 마스킹 하나뿐이라 별도 모듈이 아니라 여기 둔다(두 번째 소비처가 생기면 공용 모듈로 옮긴다).</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovRrnRules {

	private static final int[] WEIGHTS = { 2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5 };

	private EgovRrnRules() {
	}

	/**
	 * 형식 검증 — 13자리 숫자, 성별/세기 코드 1~8, 실존하는 생년월일.
	 *
	 * @param digits 숫자만(구분자 없음). null·길이 13 아님·숫자 아닌 문자 포함이면 false
	 */
	public static boolean isValidFormat(CharSequence digits) {
		if (digits == null || digits.length() != 13) {
			return false;
		}
		for (int i = 0; i < 13; i++) {
			char ch = digits.charAt(i);
			if (ch < '0' || ch > '9') {
				return false;
			}
		}
		int code = digits.charAt(6) - '0';
		if (code < 1 || code > 8) {
			return false;
		}
		int century = (code == 1 || code == 2 || code == 5 || code == 6) ? 1900 : 2000;
		int year = century + two(digits, 0);
		int month = two(digits, 2);
		int day = two(digits, 4);
		try {
			LocalDate.of(year, month, day);
			return true;
		} catch (DateTimeException e) {
			return false;
		}
	}

	/**
	 * 레거시 검증번호 검증 — {@link #isValidFormat(CharSequence)} 통과 + 구 체계 마지막 자리 일치.
	 *
	 * @param digits 숫자만(구분자 없음)
	 */
	public static boolean isValidLegacyChecksum(CharSequence digits) {
		if (!isValidFormat(digits)) {
			return false;
		}
		int sum = 0;
		for (int i = 0; i < 12; i++) {
			sum += WEIGHTS[i] * (digits.charAt(i) - '0');
		}
		int check = (11 - sum % 11) % 10;
		int code = digits.charAt(6) - '0';
		if (code >= 5) {
			check = (check + 2) % 10;
		}
		return check == digits.charAt(12) - '0';
	}

	private static int two(CharSequence digits, int from) {
		return (digits.charAt(from) - '0') * 10 + (digits.charAt(from + 1) - '0');
	}
}
