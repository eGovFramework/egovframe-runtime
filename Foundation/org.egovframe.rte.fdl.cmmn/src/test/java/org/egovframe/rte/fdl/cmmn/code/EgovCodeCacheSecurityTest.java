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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovCodeCache} 의 <b>호출자 입력에 대한 견고성</b> 검증.
 *
 * <p>코드그룹 ID·코드 값은 화면 파라미터에서 그대로 넘어오기 쉽다. 그 값이 무엇이든 캐시는
 * 원천(DB)에 가지 않고, 보관량이 늘지 않으며, 돌려준 컬렉션으로 캐시 내용을 바꿀 수 없어야 한다.
 * reload 가 원천에서 막혀 있는 동안에도 조회는 교체 전 스냅숏을 통째로 본다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.07  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovCodeCacheSecurityTest {

	private static final List<EgovCode> CODES = List.of(
			EgovCode.of("G", "A", "a"), new EgovCode("G", "B", "b", "", false, 1));

	@Test
	@DisplayName("존재하지 않는 키를 아무리 조회해도 원천 호출·보관량이 늘지 않는다")
	void 미존재_키_반복_조회() {
		AtomicInteger calls = new AtomicInteger();
		EgovCodeCache cache = new EgovCodeCache(() -> {
			calls.incrementAndGet();
			return CODES;
		});

		for (int i = 0; i < 200_000; i++) {
			String key = "no-such-" + i;
			assertTrue(cache.getCodes(key).isEmpty());
			assertTrue(cache.getActiveCodes(key).isEmpty());
			assertFalse(cache.getName(key, key).isPresent());
		}

		assertEquals(1, calls.get(), "조회는 원천에 가지 않는다");
		assertEquals(Set.of("G"), cache.getGroupIds(), "미존재 키는 보관되지 않는다");
		assertEquals(2, cache.size());
	}

	@Test
	@DisplayName("돌려준 컬렉션은 전부 불변이다 — 그룹 목록·빈 결과 포함")
	void 반환_컬렉션_불변() {
		EgovCodeCache cache = new EgovCodeCache(new EgovInMemoryCodeLoader(CODES));
		EgovCode intruder = EgovCode.of("G", "Z", "z");

		assertThrows(UnsupportedOperationException.class, () -> cache.getCodes("G").add(intruder));
		assertThrows(UnsupportedOperationException.class, () -> cache.getActiveCodes("G").clear());
		assertThrows(UnsupportedOperationException.class, () -> cache.getCodes("no-such").add(intruder));
		assertThrows(UnsupportedOperationException.class, () -> cache.getGroupIds().add("H"));
		assertThrows(UnsupportedOperationException.class, () -> cache.getGroupIds().remove("G"));

		assertEquals(2, cache.getCodes("G").size(), "시도 후에도 내용은 그대로다");
		assertEquals(Set.of("G"), cache.getGroupIds());
	}

	@Test
	@DisplayName("로더가 돌려준 목록을 나중에 바꿔도 스냅숏은 변하지 않는다")
	void 로더_목록_사후_변경() {
		List<EgovCode> mutable = new ArrayList<>(CODES);
		EgovCodeCache cache = new EgovCodeCache(() -> mutable);

		mutable.add(EgovCode.of("G", "C", "c"));
		mutable.add(EgovCode.of("H", "X", "x"));

		assertEquals(2, cache.size());
		assertEquals(Set.of("G"), cache.getGroupIds());
		assertEquals(2, cache.getCodes("G").size());
	}

	@Test
	@DisplayName("reload 가 원천에서 막혀 있는 동안 조회는 교체 전 스냅숏을 통째로 본다")
	void reload_중_조회는_기존_스냅숏() throws Exception {
		CountDownLatch entered = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		List<EgovCode> next = List.of(
				EgovCode.of("G", "N1", "n1"), EgovCode.of("G", "N2", "n2"), EgovCode.of("G", "N3", "n3"));
		EgovCodeCache cache = new EgovCodeCache(new EgovCodeLoader() {
			private int calls;

			@Override
			public List<EgovCode> loadAll() {
				if (calls++ == 0) {
					return CODES;
				}
				entered.countDown();
				try {
					release.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return next;
			}
		});

		Thread reloader = new Thread(cache::reload);
		reloader.start();
		try {
			assertTrue(entered.await(5, TimeUnit.SECONDS), "로더가 reload 안에서 호출돼야 한다");
			assertEquals(2, cache.getCodes("G").size(), "교체 전 스냅숏");
			assertEquals(2, cache.size());
		} finally {
			release.countDown();
			reloader.join(5_000);
		}

		assertEquals(3, cache.getCodes("G").size(), "교체 후 스냅숏");
		assertEquals(3, cache.size());
	}
}
