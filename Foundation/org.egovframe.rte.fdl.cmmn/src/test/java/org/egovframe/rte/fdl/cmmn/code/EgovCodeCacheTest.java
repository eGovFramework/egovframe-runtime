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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovCodeCache} 단위 테스트.
 *
 * <p>일반 동작 검증과 함께 <b>공통컴포넌트 원본(EgovCmmUseService)의 결함을 회귀로
 * 고정</b>한다 — 캐시 부재(매 조회 DAO 직행), 복수 그룹 조회의 N+1, 그리고 갱신 실패가
 * 침묵으로 넘어가는 상태를 만들지 않는다.</p>
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
class EgovCodeCacheTest {

	private static List<EgovCode> sampleCodes() {
		return Arrays.asList(
				new EgovCode("COM001", "B", "두 번째", "", true, 2),
				new EgovCode("COM001", "A", "첫 번째", "설명", true, 1),
				new EgovCode("COM001", "X", "폐기됨", "", false, 3),
				EgovCode.of("GENDER", "M", "남성"),
				EgovCode.of("GENDER", "F", "여성"));
	}

	private static EgovCodeCache cacheOf(List<EgovCode> codes) {
		return new EgovCodeCache(new EgovInMemoryCodeLoader(codes));
	}

	@Nested
	@DisplayName("원본 결함 회귀 — 공통컴포넌트 EgovCmmUseService 라면 실패한다")
	class LegacyDefectRegression {

		@Test
		@DisplayName("조회 1만 번에 원천 접근은 1번뿐이다 (원본은 매 조회 DAO 직행)")
		void 조회는_원천에_가지_않는다() {
			AtomicInteger loadCount = new AtomicInteger();
			EgovCodeLoader countingLoader = () -> {
				loadCount.incrementAndGet();
				return sampleCodes();
			};

			EgovCodeCache cache = new EgovCodeCache(countingLoader);
			for (int i = 0; i < 10_000; i++) {
				cache.getCodes("COM001");
				cache.getName("GENDER", "M");
			}

			assertEquals(1, loadCount.get(), "생성 시 적재 1번 이후 원천에 가면 안 된다");
		}

		@Test
		@DisplayName("복수 그룹 조회에 그룹 수만큼의 원천 접근이 없다 (원본 selectCmmCodeDetails 는 N+1)")
		void 복수_그룹도_원천_접근_없음() {
			AtomicInteger loadCount = new AtomicInteger();
			EgovCodeCache cache = new EgovCodeCache(() -> {
				loadCount.incrementAndGet();
				return sampleCodes();
			});

			// 원본은 그룹 목록을 돌며 그룹당 쿼리 1번씩 날렸다.
			for (String groupId : Arrays.asList("COM001", "GENDER", "COM001", "GENDER")) {
				cache.getCodes(groupId);
			}

			assertEquals(1, loadCount.get());
		}

		@Test
		@DisplayName("reload 실패 시 기존 스냅숏을 유지하고 예외로 알린다 — 침묵 실패 금지")
		void reload_실패는_기존_유지_예외() {
			AtomicBoolean fail = new AtomicBoolean(false);
			EgovCodeCache cache = new EgovCodeCache(() -> {
				if (fail.get()) {
					throw new IllegalStateException("원천 장애");
				}
				return sampleCodes();
			});

			fail.set(true);
			assertThrows(IllegalStateException.class, cache::reload);

			// 실패했지만 기존 코드는 그대로 조회된다.
			assertEquals(3, cache.getCodes("COM001").size());
			assertEquals(Optional.of("남성"), cache.getName("GENDER", "M"));
		}
	}

	@Nested
	@DisplayName("조회")
	class Lookup {

		@Test
		@DisplayName("그룹별로 묶여 정렬 순서 → 코드 순으로 나온다")
		void 그룹핑과_정렬() {
			List<EgovCode> codes = cacheOf(sampleCodes()).getCodes("COM001");

			assertEquals(Arrays.asList("A", "B", "X"),
					codes.stream().map(EgovCode::getCode).toList());
		}

		@Test
		@DisplayName("getActiveCodes 는 사용 중만 — 셀렉트박스 용도")
		void 사용중_필터() {
			List<EgovCode> active = cacheOf(sampleCodes()).getActiveCodes("COM001");

			assertEquals(Arrays.asList("A", "B"),
					active.stream().map(EgovCode::getCode).toList());
		}

		@Test
		@DisplayName("getCodes 는 사용 안 함도 포함 — 과거 데이터의 이름 표시 용도")
		void 전체_조회는_폐기_포함() {
			assertEquals(Optional.of("폐기됨"), cacheOf(sampleCodes()).getName("COM001", "X"));
		}

		@Test
		@DisplayName("코드 → 이름 지름길")
		void 이름_조회() {
			EgovCodeCache cache = cacheOf(sampleCodes());

			assertEquals(Optional.of("첫 번째"), cache.getName("COM001", "A"));
			assertEquals("A99", cache.getName("COM001", "A99").orElse("A99"));
		}

		@Test
		@DisplayName("없는 그룹·null 은 빈 결과 — 예외를 던지지 않는다")
		void 없는_그룹과_null() {
			EgovCodeCache cache = cacheOf(sampleCodes());

			assertTrue(cache.getCodes("NOPE").isEmpty());
			assertTrue(cache.getCodes(null).isEmpty());
			assertEquals(Optional.empty(), cache.getCode("COM001", null));
			assertEquals(Optional.empty(), cache.getCode(null, "A"));
		}

		@Test
		@DisplayName("반환 목록은 불변이다")
		void 반환_불변() {
			EgovCodeCache cache = cacheOf(sampleCodes());

			assertThrows(UnsupportedOperationException.class,
					() -> cache.getCodes("COM001").clear());
			assertThrows(UnsupportedOperationException.class,
					() -> cache.getGroupIds().clear());
		}

		@Test
		@DisplayName("그룹 목록과 전체 건수")
		void 집계() {
			EgovCodeCache cache = cacheOf(sampleCodes());

			assertEquals(2, cache.getGroupIds().size());
			assertEquals(5, cache.size());
		}
	}

	@Nested
	@DisplayName("적재·갱신")
	class LoadAndReload {

		@Test
		@DisplayName("생성 시 즉시 적재 — 원천 장애는 기동 실패로 드러난다(fail-fast)")
		void 생성시_적재_실패는_즉시() {
			assertThrows(IllegalStateException.class,
					() -> new EgovCodeCache(() -> {
						throw new IllegalStateException("DB down");
					}));
		}

		@Test
		@DisplayName("reload 로 스냅숏이 통째로 바뀐다")
		void reload_교체() {
			AtomicBoolean updated = new AtomicBoolean(false);
			EgovCodeCache cache = new EgovCodeCache(() -> updated.get()
					? List.of(EgovCode.of("COM001", "NEW", "신규"))
					: sampleCodes());

			updated.set(true);
			cache.reload();

			assertEquals(1, cache.size());
			assertEquals(Optional.of("신규"), cache.getName("COM001", "NEW"));
			assertTrue(cache.getCodes("GENDER").isEmpty(), "사라진 그룹은 스냅숏에서도 사라진다");
		}

		@Test
		@DisplayName("같은 그룹의 코드 중복은 데이터 결함으로 즉시 실패한다 — 조용히 덮지 않는다")
		void 중복_코드_거부() {
			List<EgovCode> duplicated = Arrays.asList(
					EgovCode.of("COM001", "A", "하나"),
					EgovCode.of("COM001", "A", "둘"));

			assertThrows(IllegalStateException.class, () -> cacheOf(duplicated));
		}

		@Test
		@DisplayName("로더가 null 을 돌려주면 즉시 실패한다")
		void 로더_null_거부() {
			assertThrows(IllegalStateException.class, () -> new EgovCodeCache(() -> null));
		}

		@Test
		@DisplayName("reload 중에도 조회는 온전한 스냅숏을 본다 — 찢어진 읽기 없음")
		void 동시성_일관_스냅숏() throws InterruptedException {
			// 두 상태를 오가며 reload 를 반복하고, 조회 스레드는 항상
			// "어느 한쪽의 완전한 상태"만 관찰해야 한다.
			AtomicBoolean flip = new AtomicBoolean(false);
			List<EgovCode> stateA = Arrays.asList(
					EgovCode.of("G", "A1", "a1"), EgovCode.of("G", "A2", "a2"));
			List<EgovCode> stateB = Arrays.asList(
					EgovCode.of("G", "B1", "b1"), EgovCode.of("G", "B2", "b2"), EgovCode.of("G", "B3", "b3"));
			EgovCodeCache cache = new EgovCodeCache(() -> flip.get() ? stateB : stateA);

			AtomicBoolean torn = new AtomicBoolean(false);
			CountDownLatch done = new CountDownLatch(1);
			ExecutorService executor = Executors.newFixedThreadPool(4);
			try {
				for (int t = 0; t < 3; t++) {
					executor.execute(() -> {
						while (done.getCount() > 0) {
							List<EgovCode> seen = cache.getCodes("G");
							if (seen.size() != 2 && seen.size() != 3) {
								torn.set(true);
							}
						}
					});
				}
				for (int i = 0; i < 200; i++) {
					flip.set(!flip.get());
					cache.reload();
				}
			} finally {
				done.countDown();
				executor.shutdown();
				assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
			}

			assertTrue(!torn.get(), "완전한 스냅숏(2건 또는 3건)만 보여야 한다");
		}
	}
}
