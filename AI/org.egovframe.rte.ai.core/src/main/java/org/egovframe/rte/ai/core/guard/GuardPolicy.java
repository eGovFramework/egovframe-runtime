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
package org.egovframe.rte.ai.core.guard;

import java.util.Locale;

/**
 * 프롬프트 인젝션 가드 정책 — 매칭 시 기록만 할지(LOG), 거부할지(BLOCK).
 *
 * <p>설정 문자열은 {@link #parse(String)} 로 바꾼다. <b>알 수 없는 값은 예외</b>다 — 참고 샘플은 오타("blok")를 기록 정책으로
 * 조용히 폴백했는데, 차단하려던 운영자가 차단되지 않는 상태로 기동되는 것은 침묵 실패다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public enum GuardPolicy {

	/** 매칭돼도 허용하되 감사 채널에 기록한다. */
	LOG,

	/** 매칭 시 거부하고 감사 채널에 기록한다. */
	BLOCK;

	/**
	 * 설정 문자열(대소문자 무관)을 정책으로 바꾼다.
	 *
	 * @throws IllegalArgumentException null·빈 값·알 수 없는 값
	 */
	public static GuardPolicy parse(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("가드 정책 값이 비어 있습니다(log 또는 block)");
		}
		switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "log":
				return LOG;
			case "block":
				return BLOCK;
			default:
				throw new IllegalArgumentException("알 수 없는 가드 정책 값입니다: '" + value + "' (log 또는 block)");
		}
	}

	/** 설정·감사 표기용 소문자 이름. */
	public String key() {
		return name().toLowerCase(Locale.ROOT);
	}
}
