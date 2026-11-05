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
package org.egovframe.rte.ai.core.audit;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI 감사 채널 <code>EGOV_AI_AUDIT</code> — 실행환경 감사 채널 4번째(SQL·SECURITY·ITL 과 같은 <code>키=값</code> 한 줄 형식).
 *
 * <p>사건 종류: 인젝션 가드 매칭(정책·허용 여부·패턴) · 색인 마스킹 적용(유형별 건수) · 모델 호출 실패(유형·예외 클래스).
 * <b>질의 원문·문서 본문·마스킹 전후 값·표본은 어떤 사건에도 기록하지 않는다</b> — 감사 채널이 2차 유출원이 되지 않게
 * 비식별 정보(유형·건수·패턴·예외 클래스)만 쓴다.</p>
 *
 * <p>로깅 구성에서 이 채널을 선언해야 발신이 보존된다(기본 root=WARN 이면 INFO 사건이 걸러진다):</p>
 * <pre>
 * &lt;Logger name="EGOV_AI_AUDIT" level="info" additivity="false"&gt;&lt;AppenderRef ref="aiAudit"/&gt;&lt;/Logger&gt;
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovAiAudit {

	/** AI 감사 전용 로거 이름. */
	public static final String LOGGER_NAME = "EGOV_AI_AUDIT";

	private static final Logger AUDIT = LoggerFactory.getLogger(LOGGER_NAME);

	private EgovAiAudit() {
	}

	/**
	 * 인젝션 가드 매칭 사건. 차단(allowed=false)은 WARN, 기록 정책의 허용은 INFO.
	 *
	 * @param policy 정책 이름(log·block)
	 * @param allowed 허용 여부
	 * @param pattern 매칭된 렉시콘 패턴(사용자 질의가 아니라 렉시콘 항목)
	 */
	public static void guard(String policy, boolean allowed, String pattern) {
		if (allowed) {
			AUDIT.info("event=guard policy={} allowed=true pattern={}", policy, sanitize(pattern));
		} else {
			AUDIT.warn("event=guard policy={} allowed=false pattern={}", policy, sanitize(pattern));
		}
	}

	/**
	 * 색인 마스킹 적용 사건 — 유형별 건수만(값 없음). 건수가 전부 0 이면 발신하지 않는다.
	 *
	 * @param counts 유형(열거형 이름 또는 키) → 치환 건수
	 */
	public static void masking(Map<?, Integer> counts) {
		if (counts == null || counts.isEmpty()) {
			return;
		}
		int total = 0;
		Map<String, Integer> ordered = new TreeMap<>();
		for (Map.Entry<?, Integer> entry : counts.entrySet()) {
			int count = entry.getValue() == null ? 0 : entry.getValue();
			total += count;
			ordered.put(String.valueOf(entry.getKey()).toLowerCase(), count);
		}
		if (total == 0) {
			return;
		}
		StringBuilder line = new StringBuilder("event=masking");
		for (Map.Entry<String, Integer> entry : ordered.entrySet()) {
			line.append(' ').append(entry.getKey()).append('=').append(entry.getValue());
		}
		line.append(" total=").append(total);
		AUDIT.info(line.toString());
	}

	/**
	 * 모델 호출 실패 사건 — 유형과 예외 클래스만(메시지는 자격 증명·URL 이 섞일 수 있어 기록하지 않는다).
	 *
	 * @param type 분류된 실패 유형 이름
	 * @param error 예외(null 가능)
	 */
	public static void callFailure(String type, Throwable error) {
		AUDIT.warn("event=call-failure type={} error={}", type,
				error == null ? "unknown" : error.getClass().getSimpleName());
	}

	/** 한 줄 형식을 지키도록 개행·제어 문자를 공백으로 바꾼다. */
	private static String sanitize(String value) {
		if (value == null) {
			return "";
		}
		StringBuilder out = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			out.append(Character.isISOControl(ch) ? ' ' : ch);
		}
		return out.toString();
	}
}
