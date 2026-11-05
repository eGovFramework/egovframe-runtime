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
package org.egovframe.rte.ai.core.text;

/**
 * 정규화 결과 — 결과 텍스트와 변경 여부, 원문 길이.
 *
 * <p>바뀐 것이 없으면 {@link #text()} 는 입력과 같은 객체이고 {@link #changed()} 는 거짓이다. 호출 측(어댑터)은 변경 시에만
 * 메타데이터(원문·결과 길이)를 기록한다.</p>
 *
 * @param text 결과 텍스트
 * @param changed 내용이 바뀌었는가
 * @param originalLength 원문 길이(문자 수)
 * @author 실행환경 개발팀
 * @since 5.1
 */
public record EgovNormalized(String text, boolean changed, int originalLength) {

	/** 결과 길이(문자 수). */
	public int length() {
		return text == null ? 0 : text.length();
	}
}
