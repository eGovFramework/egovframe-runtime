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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.egovframe.rte.ai.core.audit.EgovAiAudit;

/**
 * 프롬프트 인젝션 가드 — 사용자 질의가 모델 지시를 뒤엎으려는 명령형 메타 지시를 담았는지 렉시콘으로 판정한다.
 *
 * <p>판정은 <b>원본 질의를 바꾸지 않고</b> 판정용 사본에서 한다: 유니코드 서식 문자(Cf)·ISO 제어 문자 제거 → NFKC → 소문자 →
 * 글자 사이 공백 제거 → 반복 문자 축약. 제로폭 공백·전각·자간 공백·글자 늘이기 같은 우회를 흡수한다. 정책은
 * {@link GuardPolicy#LOG}(허용+기록)·{@link GuardPolicy#BLOCK}(거부+기록)이며 매칭 시 {@link EgovAiAudit} 에 정책·허용 여부·
 * 패턴만 남긴다(질의 원문 미기록).</p>
 *
 * <p>어댑터는 이 판정을 프레임워크 확장점(Spring AI Advisor·LangChain4j InputGuardrail)에 얹는다. 렉시콘은 명령형 메타 지시만
 * 담아 오탐을 줄인 것이므로("지시"·"무시" 단어 하나로는 매칭하지 않음) 의미 기반 탐지가 아니라 표기 기반 1차 방어다.</p>
 *
 * <pre>
 * EgovPromptInjectionGuard guard = EgovPromptInjectionGuard.of(GuardPolicy.BLOCK, EgovInjectionLexicon.seed());
 * GuardDecision decision = guard.inspect(userQuery);
 * if (!decision.allowed()) { ... 안내 문구 반환 ... }
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플 가드의 정규화 규칙 차용, 렉시콘 부재·정책 오류의
 *                            침묵 폴백 교정, 감사 채널 발신)
 *  2026.09.08  실행환경팀     보안 재점검(5차): 결합 문자·변형 선택자 삽입 우회 — NFKD 뒤 Mn/Mc/Me 제거 후 NFKC(CWE-693)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovPromptInjectionGuard {

	/**
	 * 글자·숫자 사이에 낀 공백·구두점·기호 — 공백만 지우면 {@code i-g-n-o-r-e}·{@code 이.전.지.시} 같은 구분자 삽입으로
	 * 렉시콘이 통째로 우회된다(2026-09-07 보안 점검). 렉시콘 패턴도 같은 정규화를 거치므로 매칭 기준은 대칭이다.
	 */
	private static final Pattern INTER_CHARACTER_WHITESPACE =
			Pattern.compile("(?<=[\\p{L}\\p{N}])[\\s\\p{P}\\p{S}]+(?=[\\p{L}\\p{N}])");
	/**
	 * 글자 사이에 낀 숫자 — 숫자는 위 구분자 제거의 경계 문자라 {@code i1g1n1o1r1e}·{@code 이2전3지4시} 처럼 끼워 넣으면
	 * 글자가 이어지지 않아 렉시콘이 통째로 우회된다(2026-09-07 보안 재점검). 렉시콘도 같은 정규화를 거치므로 대칭이다.
	 */
	private static final Pattern INTER_LETTER_DIGITS = Pattern.compile("(?<=\\p{L})\\p{N}+(?=\\p{L})");
	private static final Pattern REPEATED_CHARACTER = Pattern.compile("(.)\\1+");

	private final GuardPolicy policy;
	private final EgovInjectionLexicon lexicon;
	private final List<String> normalizedPatterns;

	private EgovPromptInjectionGuard(GuardPolicy policy, EgovInjectionLexicon lexicon) {
		this.policy = Objects.requireNonNull(policy, "policy must not be null");
		this.lexicon = Objects.requireNonNull(lexicon, "lexicon must not be null");
		List<String> normalized = new ArrayList<>(lexicon.size());
		for (String pattern : lexicon.patterns()) {
			String n = normalizeForDetection(pattern);
			if (!n.isEmpty()) {
				normalized.add(n);
			}
		}
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("정규화 뒤 남는 렉시콘 패턴이 없습니다");
		}
		this.normalizedPatterns = List.copyOf(normalized);
	}

	/**
	 * 가드를 만든다.
	 *
	 * @param policy 정책
	 * @param lexicon 렉시콘(시드 + 확장)
	 */
	public static EgovPromptInjectionGuard of(GuardPolicy policy, EgovInjectionLexicon lexicon) {
		return new EgovPromptInjectionGuard(policy, lexicon);
	}

	/** 적용 정책. */
	public GuardPolicy policy() {
		return policy;
	}

	/** 렉시콘. */
	public EgovInjectionLexicon lexicon() {
		return lexicon;
	}

	/**
	 * 질의를 판정한다. 매칭되면 감사 채널에 기록한다(질의 원문은 기록하지 않는다).
	 *
	 * @param rawQuery 원본 질의(null·빈 값은 허용·비매칭)
	 * @return 판정 결과
	 */
	public GuardDecision inspect(String rawQuery) {
		String normalizedQuery = normalizeForDetection(rawQuery);
		if (normalizedQuery.isEmpty()) {
			return GuardDecision.clean(policy);
		}
		for (int i = 0; i < normalizedPatterns.size(); i++) {
			if (normalizedQuery.contains(normalizedPatterns.get(i))) {
				String pattern = lexicon.patterns().get(i);
				boolean allowed = policy != GuardPolicy.BLOCK;
				EgovAiAudit.guard(policy.key(), allowed, pattern);
				return new GuardDecision(allowed, true, policy, pattern);
			}
		}
		return GuardDecision.clean(policy);
	}

	/**
	 * 판정용 정규화 — 서식·제어 문자 제거, NFKD 뒤 결합 문자 제거, NFKC, 소문자, 글자 사이 공백·구두점·기호·숫자 제거, 반복 문자 축약.
	 *
	 * @param text 원본(null 이면 빈 문자열)
	 * @return 정규화 사본
	 */
	public static String normalizeForDetection(String text) {
		if (text == null) {
			return "";
		}
		StringBuilder filtered = new StringBuilder(text.length());
		text.codePoints().filter(cp -> !isRemoved(cp)).forEach(filtered::appendCodePoint);
		// 결합 문자(Mn/Mc/Me — 다이어크리틱·변형 선택자)는 완전 분해(NFKD) 뒤에 벗겨야 한다: NFKC 는 "i"+U+0301 을 "í" 로
		// 합성해 글자를 바꾸고, 한글은 완성형이 없어 결합 문자가 그대로 남아 렉시콘과 어긋난다(CWE-693, 2026-09-08 보안 재점검).
		// 벗긴 뒤 NFKC 로 재합성한다(한글 자모 → 음절, 전각 → 반각). 렉시콘도 같은 함수로 정규화되므로 판정은 일관된다.
		String decomposed = Normalizer.normalize(filtered, Normalizer.Form.NFKD);
		StringBuilder stripped = new StringBuilder(decomposed.length());
		decomposed.codePoints().filter(cp -> !isCombiningMark(cp)).forEach(stripped::appendCodePoint);
		String normalized = Normalizer.normalize(stripped, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
		normalized = INTER_CHARACTER_WHITESPACE.matcher(normalized).replaceAll("");
		normalized = INTER_LETTER_DIGITS.matcher(normalized).replaceAll("");
		return REPEATED_CHARACTER.matcher(normalized).replaceAll("$1");
	}

	/** 유니코드 Cf(FORMAT) 부류 전체와 ISO 제어 문자 — 제로폭 공백·BOM·WORD JOINER·방향 제어 우회를 흡수한다. */
	private static boolean isRemoved(int codePoint) {
		return Character.getType(codePoint) == Character.FORMAT || Character.isISOControl(codePoint);
	}

	/** 결합 문자 — 비공백 결합(Mn, 변형 선택자 포함)·공백 결합(Mc)·둘러싸는(Me) 마크. 판정 사본에서만 벗겨낸다. */
	private static boolean isCombiningMark(int codePoint) {
		int type = Character.getType(codePoint);
		return type == Character.NON_SPACING_MARK || type == Character.COMBINING_SPACING_MARK
				|| type == Character.ENCLOSING_MARK;
	}
}
