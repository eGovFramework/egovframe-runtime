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
package org.egovframe.rte.fdl.cmmn.code;

import java.util.Objects;

/**
 * 공통코드 한 항목을 담는 <b>불변</b> 값 객체.
 *
 * <p><b>NOTE:</b> 코드그룹({@code groupId}) 아래에 코드({@code code})와 표시명({@code name})이
 * 달리는 전형적인 코드 테이블 구조를 일반화한 것이다. 특정 테이블·업무에 매이지 않도록
 * 필드를 기술 코어(그룹·코드·이름·설명·사용여부·정렬순서)로 한정했다.</p>
 *
 * <p>동일성은 <b>그룹 + 코드</b>로 판정한다 — 같은 그룹 안에서 코드는 유일하다는 것이
 * 코드 테이블의 통상 계약이다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovCode {

	private final String groupId;

	private final String code;

	private final String name;

	private final String description;

	private final boolean enabled;

	private final int sortOrder;

	/**
	 * 전체 필드를 지정해 생성한다.
	 *
	 * @param groupId     코드그룹 ID(필수)
	 * @param code        코드 값(필수)
	 * @param name        표시명(필수)
	 * @param description 설명({@code null} 이면 빈 문자열)
	 * @param enabled     사용 여부
	 * @param sortOrder   정렬 순서(작을수록 앞)
	 */
	public EgovCode(String groupId, String code, String name,
			String description, boolean enabled, int sortOrder) {
		this.groupId = requireText(groupId, "groupId");
		this.code = requireText(code, "code");
		this.name = requireText(name, "name");
		this.description = (description == null) ? "" : description;
		this.enabled = enabled;
		this.sortOrder = sortOrder;
	}

	/**
	 * 필수 필드만으로 생성한다(사용 중 · 정렬 0 · 설명 없음).
	 *
	 * @param groupId 코드그룹 ID
	 * @param code    코드 값
	 * @param name    표시명
	 * @return 코드 항목
	 */
	public static EgovCode of(String groupId, String code, String name) {
		return new EgovCode(groupId, code, name, "", true, 0);
	}

	/**
	 * 코드그룹 ID 를 반환한다.
	 *
	 * @return 코드그룹 ID
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * 코드 값을 반환한다.
	 *
	 * @return 코드 값
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 표시명을 반환한다.
	 *
	 * @return 표시명
	 */
	public String getName() {
		return name;
	}

	/**
	 * 설명을 반환한다(없으면 빈 문자열).
	 *
	 * @return 설명
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 사용 여부를 반환한다.
	 *
	 * @return 사용 중이면 {@code true}
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * 정렬 순서를 반환한다(작을수록 앞).
	 *
	 * @return 정렬 순서
	 */
	public int getSortOrder() {
		return sortOrder;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EgovCode)) {
			return false;
		}
		EgovCode other = (EgovCode) obj;
		return groupId.equals(other.groupId) && code.equals(other.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupId, code);
	}

	@Override
	public String toString() {
		return "EgovCode[" + groupId + ":" + code + "=" + name
				+ (enabled ? "" : " (disabled)") + "]";
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " must not be null or empty");
		}
		return value;
	}
}
