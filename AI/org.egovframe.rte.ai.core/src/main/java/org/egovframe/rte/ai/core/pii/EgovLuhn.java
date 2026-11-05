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

/**
 * Luhn(모듈러스 10) 검사 — 카드번호 후보의 오탐 억제.
 *
 * <p>소비처가 색인 마스킹 하나뿐이라 공용 모듈이 아니라 여기 둔다(헌장 ② — 두 번째 소비처가 생기면 이동 규칙 2로
 * 옮긴다).</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovLuhn {

	private EgovLuhn() {
	}

	/**
	 * 숫자열이 Luhn 검사를 통과하는가.
	 *
	 * @param digits 숫자만(구분자 없음). null·2자 미만·숫자 아닌 문자 포함이면 false
	 */
	public static boolean isValid(CharSequence digits) {
		if (digits == null || digits.length() < 2) {
			return false;
		}
		int sum = 0;
		boolean doubleDigit = false;
		for (int i = digits.length() - 1; i >= 0; i--) {
			char ch = digits.charAt(i);
			if (ch < '0' || ch > '9') {
				return false;
			}
			int d = ch - '0';
			if (doubleDigit) {
				d *= 2;
				if (d > 9) {
					d -= 9;
				}
			}
			sum += d;
			doubleDigit = !doubleDigit;
		}
		return sum % 10 == 0;
	}
}
