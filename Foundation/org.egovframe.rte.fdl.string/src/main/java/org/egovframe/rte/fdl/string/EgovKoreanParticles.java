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
package org.egovframe.rte.fdl.string;

/**
 * 단어의 받침 유무에 맞는 한글 조사를 골라 주는 유틸 클래스.
 *
 * <p><b>NOTE:</b> 메시지 템플릿에서 "{이름}는" 처럼 조사를 고정하면 "홍길동는" 같은 어색한
 * 문장이 된다. 마지막 글자의 종성 유무로 다섯 쌍의 조사를 고른다 —
 * 은/는 · 이/가 · 을/를 · 과/와 · 으로/로.</p>
 *
 * <pre>
 * EgovKoreanParticles.attach("홍길동", Particle.TOPIC)     → "홍길동은"
 * EgovKoreanParticles.attach("나이", Particle.TOPIC)       → "나이는"
 * EgovKoreanParticles.attach("서울", Particle.TO)          → "서울로"     (ㄹ 받침 특칙)
 * EgovKoreanParticles.particleOf("책상", Particle.SUBJECT) → "이"        (조사만)
 * </pre>
 *
 * <p><b>계약</b> — 한글 음절(가~힣)이 아닌 글자로 끝나는 단어(영문·숫자·기호)는 <b>받침이
 * 없는 것으로 간주</b>한다("MOU가"·"3를"). 소리 나는 대로 판정하려면 발음 사전이 필요해
 * 유틸의 범위를 벗어난다 — 그런 자리는 호출부가 조사를 직접 지정한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovStringUtil 조사 3종(2024.10)의
 *                            종성 판정을 차용하되 재구성 — 원본은 "조사를 반환한다"는 이름
 *                            (getSubjectParticle)과 달리 명사+조사 전체를 돌려줘 이름과 동작이
 *                            어긋났고, 과/와·으로/로(ㄹ 받침 특칙)가 없었다. 코드 이식 아님)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovKoreanParticles {

	/** 한글 음절 시작(가). */
	private static final char HANGUL_BASE = 0xAC00;

	/** 한글 음절 끝(힣). */
	private static final char HANGUL_LAST = 0xD7A3;

	/** 종성 개수 — 음절 코드값을 이 값으로 나눈 나머지가 0 이면 받침이 없다. */
	private static final int FINAL_CONSONANT_COUNT = 28;

	/** 종성 ㄹ 의 색인 — 으로/로 특칙에 쓴다. */
	private static final int FINAL_RIEUL = 8;

	private EgovKoreanParticles() {
	}

	/**
	 * 단어에 맞는 조사를 붙여 돌려준다.
	 *
	 * @param word     단어({@code null} 이면 {@code null})
	 * @param particle 조사 종류
	 * @return 단어 + 조사({@code "홍길동은"})
	 */
	public static String attach(String word, Particle particle) {
		if (word == null) {
			return null;
		}
		requireParticle(particle);
		return word + particle.select(word);
	}

	/**
	 * 단어에 맞는 <b>조사만</b> 돌려준다 — 템플릿 엔진에서 조사 자리만 채울 때 쓴다.
	 *
	 * @param word     단어({@code null} 이면 받침 없는 쪽 조사)
	 * @param particle 조사 종류
	 * @return 조사({@code "은"} 또는 {@code "는"})
	 */
	public static String particleOf(String word, Particle particle) {
		requireParticle(particle);
		return particle.select(word);
	}

	/**
	 * 단어가 받침으로 끝나는지 판정한다(한글 음절 밖의 글자는 받침 없음으로 간주).
	 *
	 * @param word 단어({@code null}·빈 문자열은 받침 없음)
	 * @return 받침이 있으면 {@code true}
	 */
	public static boolean endsWithFinalConsonant(String word) {
		return finalConsonantIndexOf(word) > 0;
	}

	/**
	 * 마지막 글자의 종성 색인을 돌려준다(0 = 받침 없음, 한글 음절 밖 = 0).
	 */
	private static int finalConsonantIndexOf(String word) {
		if (word == null || word.isEmpty()) {
			return 0;
		}
		char last = word.charAt(word.length() - 1);
		if (last < HANGUL_BASE || last > HANGUL_LAST) {
			return 0;
		}
		return (last - HANGUL_BASE) % FINAL_CONSONANT_COUNT;
	}

	private static void requireParticle(Particle particle) {
		if (particle == null) {
			throw new IllegalArgumentException("particle must not be null");
		}
	}

	/**
	 * 조사 종류 — 받침 있음/없음 한 쌍씩.
	 */
	public enum Particle {

		/** 보조사 은/는. */
		TOPIC("은", "는"),

		/** 주격 이/가. */
		SUBJECT("이", "가"),

		/** 목적격 을/를. */
		OBJECT("을", "를"),

		/** 접속 과/와. */
		AND("과", "와"),

		/**
		 * 방향·수단 으로/로 — <b>ㄹ 받침은 "로"</b> 를 쓴다("서울로", "연필로").
		 */
		TO("으로", "로") {
			@Override
			String select(String word) {
				int index = finalConsonantIndexOf(word);
				return (index == 0 || index == FINAL_RIEUL) ? withoutFinal() : withFinal();
			}
		};

		private final String withFinal;

		private final String withoutFinal;

		Particle(String withFinal, String withoutFinal) {
			this.withFinal = withFinal;
			this.withoutFinal = withoutFinal;
		}

		/** 받침 있는 단어에 붙는 쪽. */
		String withFinal() {
			return withFinal;
		}

		/** 받침 없는 단어에 붙는 쪽. */
		String withoutFinal() {
			return withoutFinal;
		}

		/** 단어에 맞는 쪽을 고른다. */
		String select(String word) {
			return endsWithFinalConsonant(word) ? withFinal : withoutFinal;
		}
	}
}
