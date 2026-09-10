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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 공통코드를 <b>불변 스냅숏</b>으로 보관하는 조회 캐시.
 *
 * <p><b>NOTE:</b> 공통코드는 읽기가 압도적이고 변경이 드물며 전체가 메모리에 들어가는
 * 크기다. 그래서 항목별 TTL 캐시가 아니라 <b>전량 스냅숏 + 명시적 {@link #reload()}</b> 로
 * 다룬다 — 조회는 락 없이 즉답하고, 운영 중 코드 변경은 reload 한 번으로 통째 반영된다
 * (프로퍼티 {@code refreshPropertyFiles}·접근제어 색인 스냅숏과 같은 실행환경 관례다).</p>
 *
 * <pre>
 * // 구성 — 생성 시점에 전량 적재된다(원천 장애는 기동 실패로 드러난다)
 * EgovCodeCache cache = new EgovCodeCache(
 *         new EgovJdbcCodeLoader(dataSource, CODE_SQL));
 *
 * // 조회 — DB 왕복 없음
 * List&lt;EgovCode&gt; codes = cache.getActiveCodes("COM001");          // 셀렉트박스
 * String name = cache.getName("COM001", "A01").orElse("A01");     // 코드 → 이름
 *
 * // 운영 중 코드 변경 시
 * cache.reload();
 * </pre>
 *
 * <p><b>reload 실패는 기존 스냅숏을 유지한 채 예외로 알린다.</b> 원천이 잠시 죽었다고
 * 멀쩡히 서비스 중인 코드가 사라지면 안 되고, 실패를 삼켜 "갱신된 줄 아는" 상태가 되어도
 * 안 되기 때문이다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovCmmUseService 의 역할을
 *                            차용하되 캐시 부재(소비처 54곳이 매 화면 DB 조회)·복수 그룹
 *                            조회의 N+1 을 전량 스냅숏으로 재구성. 특정 테이블·VO 종속은
 *                            로더 SPI 로 분리 — 코드 이식 아님)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public class EgovCodeCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovCodeCache.class);

	/** 그룹 내 정렬 — 정렬 순서, 같으면 코드 값. */
	private static final Comparator<EgovCode> GROUP_ORDER =
			Comparator.comparingInt(EgovCode::getSortOrder).thenComparing(EgovCode::getCode);

	private final EgovCodeLoader loader;

	private volatile Snapshot snapshot;

	/**
	 * 캐시를 만들고 <b>즉시 전량 적재한다</b> — 원천 장애가 기동 실패로 드러난다(fail-fast).
	 *
	 * @param loader 코드 로더(필수)
	 */
	public EgovCodeCache(EgovCodeLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException("loader must not be null");
		}
		this.loader = loader;
		this.snapshot = Snapshot.from(loader.loadAll());
		LOGGER.info("공통코드 캐시 적재 완료 — 그룹 {}개, 코드 {}건",
				snapshot.groups.size(), snapshot.totalCount);
	}

	/**
	 * 그룹의 코드 전부를 반환한다(<b>사용 안 함 포함</b>, 정렬 순서 → 코드 순).
	 *
	 * <p>과거 데이터의 코드 이름 표시처럼 사용 안 함 코드도 필요한 자리를 위한 것이다.
	 * 셀렉트박스처럼 사용 중만 필요하면 {@link #getActiveCodes(String)} 를 쓴다.</p>
	 *
	 * @param groupId 코드그룹 ID({@code null} 허용)
	 * @return 불변 목록(그룹이 없으면 빈 목록)
	 */
	public List<EgovCode> getCodes(String groupId) {
		GroupEntry entry = snapshot.groups.get(groupId);
		return (entry == null) ? Collections.emptyList() : entry.all;
	}

	/**
	 * 그룹의 <b>사용 중</b> 코드만 반환한다(정렬 순서 → 코드 순).
	 *
	 * @param groupId 코드그룹 ID({@code null} 허용)
	 * @return 불변 목록(그룹이 없으면 빈 목록)
	 */
	public List<EgovCode> getActiveCodes(String groupId) {
		GroupEntry entry = snapshot.groups.get(groupId);
		return (entry == null) ? Collections.emptyList() : entry.active;
	}

	/**
	 * 코드 항목 하나를 찾는다.
	 *
	 * @param groupId 코드그룹 ID({@code null} 허용)
	 * @param code    코드 값({@code null} 허용)
	 * @return 항목. 없으면 {@link Optional#empty()}
	 */
	public Optional<EgovCode> getCode(String groupId, String code) {
		GroupEntry entry = snapshot.groups.get(groupId);
		return (entry == null || code == null)
				? Optional.empty() : Optional.ofNullable(entry.byCode.get(code));
	}

	/**
	 * 코드의 표시명을 찾는다 — 가장 흔한 용도(코드 → 이름)의 지름길이다.
	 *
	 * @param groupId 코드그룹 ID({@code null} 허용)
	 * @param code    코드 값({@code null} 허용)
	 * @return 표시명. 없으면 {@link Optional#empty()} — 원본 코드 값을 그대로 내보내는
	 *         폴백은 호출부가 {@code orElse(code)} 로 명시한다
	 */
	public Optional<String> getName(String groupId, String code) {
		return getCode(groupId, code).map(EgovCode::getName);
	}

	/**
	 * 보관 중인 코드그룹 ID 전부를 반환한다.
	 *
	 * @return 불변 집합
	 */
	public Set<String> getGroupIds() {
		return snapshot.groups.keySet();
	}

	/**
	 * 보관 중인 전체 코드 수를 반환한다(사용 안 함 포함).
	 *
	 * @return 코드 수
	 */
	public int size() {
		return snapshot.totalCount;
	}

	/**
	 * 원천에서 전량을 다시 읽어 스냅숏을 <b>원자적으로 교체</b>한다.
	 *
	 * <p>교체 전까지 조회는 기존 스냅숏으로 계속 응답하므로 무중단이다. 적재가 실패하면
	 * <b>기존 스냅숏을 유지한 채</b> 예외를 던진다 — 부분 반영이나 침묵 실패는 없다.</p>
	 *
	 * @throws RuntimeException 로더가 던진 예외 그대로(원천 장애·데이터 결함)
	 */
	public synchronized void reload() {
		Snapshot next = Snapshot.from(loader.loadAll());
		this.snapshot = next;
		LOGGER.info("공통코드 캐시 갱신 완료 — 그룹 {}개, 코드 {}건",
				next.groups.size(), next.totalCount);
	}

	/** 한 그룹의 코드들 — 전체·사용 중·코드별 색인을 미리 만들어 둔다. */
	private static final class GroupEntry {

		private final List<EgovCode> all;

		private final List<EgovCode> active;

		private final Map<String, EgovCode> byCode;

		private GroupEntry(List<EgovCode> sorted) {
			List<EgovCode> activeOnly = new ArrayList<>();
			Map<String, EgovCode> index = new LinkedHashMap<>();
			for (EgovCode code : sorted) {
				EgovCode duplicated = index.put(code.getCode(), code);
				if (duplicated != null) {
					// (그룹, 코드) 중복은 원천 데이터 결함이다 — 한쪽을 조용히 덮지 않는다.
					throw new IllegalStateException("duplicate code in group '"
							+ code.getGroupId() + "': " + code.getCode());
				}
				if (code.isEnabled()) {
					activeOnly.add(code);
				}
			}
			this.all = Collections.unmodifiableList(sorted);
			this.active = Collections.unmodifiableList(activeOnly);
			this.byCode = Collections.unmodifiableMap(index);
		}
	}

	/** 불변 스냅숏 — 참조 교체만으로 전체가 갱신된다. */
	private static final class Snapshot {

		private final Map<String, GroupEntry> groups;

		private final int totalCount;

		private Snapshot(Map<String, GroupEntry> groups, int totalCount) {
			this.groups = Collections.unmodifiableMap(groups);
			this.totalCount = totalCount;
		}

		private static Snapshot from(List<EgovCode> codes) {
			if (codes == null) {
				throw new IllegalStateException("code loader returned null — return an empty list instead");
			}
			Map<String, List<EgovCode>> grouped = new LinkedHashMap<>();
			for (EgovCode code : codes) {
				if (code == null) {
					throw new IllegalStateException("code loader returned a null element");
				}
				grouped.computeIfAbsent(code.getGroupId(), key -> new ArrayList<>()).add(code);
			}
			Map<String, GroupEntry> groups = new LinkedHashMap<>();
			for (Map.Entry<String, List<EgovCode>> entry : grouped.entrySet()) {
				entry.getValue().sort(GROUP_ORDER);
				groups.put(entry.getKey(), new GroupEntry(entry.getValue()));
			}
			return new Snapshot(groups, codes.size());
		}
	}
}
