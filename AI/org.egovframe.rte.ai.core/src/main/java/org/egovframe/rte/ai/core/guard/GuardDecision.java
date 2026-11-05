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

/**
 * 가드 판정 결과.
 *
 * @param allowed 질의를 계속 처리해도 되는가
 * @param matched 렉시콘 패턴이 매칭됐는가
 * @param policy 적용된 정책
 * @param matchedPattern 매칭된 패턴(없으면 null)
 * @author 실행환경 개발팀
 * @since 5.1
 */
public record GuardDecision(boolean allowed, boolean matched, GuardPolicy policy, String matchedPattern) {

	/** 매칭 없음(허용). */
	static GuardDecision clean(GuardPolicy policy) {
		return new GuardDecision(true, false, policy, null);
	}
}
