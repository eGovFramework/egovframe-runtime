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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.egovframe.rte.fdl.string.EgovKoreanParticles.Particle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovKoreanParticles} 단위 테스트.
 *
 * <p><b>공통컴포넌트 원본(EgovStringUtil 조사 3종)의 이름-동작 불일치를 회귀로 고정</b>한다 —
 * 원본 {@code getSubjectParticle} 은 "조사를 반환한다"는 이름과 달리 명사+조사 전체를
 * 돌려줬다. 여기서는 {@code attach}(단어+조사)와 {@code particleOf}(조사만)로 분리한다.</p>
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
class EgovKoreanParticlesTest {

	@Nested
	@DisplayName("이름과 동작의 일치 — 원본의 불일치를 바로잡는다")
	class NameContract {

		@Test
		@DisplayName("particleOf 는 조사만 돌려준다 (원본 getXxxParticle 은 명사+조사를 돌려줬다)")
		void 조사만_반환() {
			assertEquals("은", EgovKoreanParticles.particleOf("홍길동", Particle.TOPIC));
			assertEquals("가", EgovKoreanParticles.particleOf("나이", Particle.SUBJECT));
		}

		@Test
		@DisplayName("attach 는 단어+조사를 돌려준다")
		void 단어_조사_반환() {
			assertEquals("홍길동은", EgovKoreanParticles.attach("홍길동", Particle.TOPIC));
			assertEquals("나이는", EgovKoreanParticles.attach("나이", Particle.TOPIC));
		}
	}

	@Nested
	@DisplayName("다섯 쌍의 조사")
	class FiveParticles {

		@Test
		@DisplayName("은/는")
		void 은는() {
			assertEquals("책상은", EgovKoreanParticles.attach("책상", Particle.TOPIC));
			assertEquals("의자는", EgovKoreanParticles.attach("의자", Particle.TOPIC));
		}

		@Test
		@DisplayName("이/가")
		void 이가() {
			assertEquals("책상이", EgovKoreanParticles.attach("책상", Particle.SUBJECT));
			assertEquals("청소기가", EgovKoreanParticles.attach("청소기", Particle.SUBJECT));
		}

		@Test
		@DisplayName("을/를")
		void 을를() {
			assertEquals("책상을", EgovKoreanParticles.attach("책상", Particle.OBJECT));
			assertEquals("청소기를", EgovKoreanParticles.attach("청소기", Particle.OBJECT));
		}

		@Test
		@DisplayName("과/와 — 원본에 없던 쌍")
		void 과와() {
			assertEquals("책상과", EgovKoreanParticles.attach("책상", Particle.AND));
			assertEquals("의자와", EgovKoreanParticles.attach("의자", Particle.AND));
		}

		@Test
		@DisplayName("으로/로 — ㄹ 받침은 '로'(원본에 없던 특칙)")
		void 으로로_ㄹ_특칙() {
			assertEquals("집으로", EgovKoreanParticles.attach("집", Particle.TO));
			assertEquals("학교로", EgovKoreanParticles.attach("학교", Particle.TO));
			assertEquals("서울로", EgovKoreanParticles.attach("서울", Particle.TO));
			assertEquals("연필로", EgovKoreanParticles.attach("연필", Particle.TO));
		}
	}

	@Nested
	@DisplayName("경계와 계약")
	class Contract {

		@Test
		@DisplayName("받침 판정")
		void 받침_판정() {
			assertTrue(EgovKoreanParticles.endsWithFinalConsonant("책상"));
			assertFalse(EgovKoreanParticles.endsWithFinalConsonant("의자"));
			assertFalse(EgovKoreanParticles.endsWithFinalConsonant(""));
			assertFalse(EgovKoreanParticles.endsWithFinalConsonant(null));
		}

		@Test
		@DisplayName("비한글 끝 글자는 받침 없음으로 간주 — 계약을 그대로 유지")
		void 비한글() {
			assertEquals("MOU를", EgovKoreanParticles.attach("MOU", Particle.OBJECT));
			assertEquals("3와 5", EgovKoreanParticles.attach("3", Particle.AND) + " 5");   // 비한글=받침 없음 계약
		}

		@Test
		@DisplayName("null 단어 — attach 는 null, particleOf 는 받침 없는 쪽")
		void null_단어() {
			assertNull(EgovKoreanParticles.attach(null, Particle.TOPIC));
			assertEquals("는", EgovKoreanParticles.particleOf(null, Particle.TOPIC));
		}

		@Test
		@DisplayName("particle 이 null 이면 즉시 실패")
		void null_조사() {
			assertThrows(IllegalArgumentException.class,
					() -> EgovKoreanParticles.attach("책상", null));
		}
	}
}
