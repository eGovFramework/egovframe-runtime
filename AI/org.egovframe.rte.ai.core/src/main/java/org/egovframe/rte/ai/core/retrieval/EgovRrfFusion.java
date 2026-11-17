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
package org.egovframe.rte.ai.core.retrieval;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RRF(Reciprocal Rank Fusion) 융합 — 여러 검색 채널의 순위를 하나로 합친다.
 *
 * <pre>
 *   score(d) = Σ_channel  weight / (k + rank)      (rank 는 0-base, k 기본 60)
 * </pre>
 *
 * <p>외부 의존 없는 순수 함수이며 결정적이다 — 동점은 키의 등장 순서(밀집 채널 먼저)로 정한다. 어떤 AI 프레임워크의
 * 문서 타입도 모르므로 어댑터가 식별자 순위 리스트로 바꿔 넘긴다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 두 샘플이 동일 사본으로 갖던 융합기의 단일 원천)
 *  2026.09.03  실행환경팀     fuseScored 추가 (과제 23 add-ai-springai — 어댑터가 RRF 점수를 문서 score 에 싣도록 점수 동반 반환)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovRrfFusion {

	/** RRF 표준 상수 — 상위 순위 항목의 영향력을 완화한다. */
	public static final int DEFAULT_K = 60;

	private EgovRrfFusion() {
	}

	/**
	 * 두 채널(밀집·어휘)의 순위를 융합한다.
	 *
	 * @param denseRanking 밀집 채널 순위(상위가 앞, null 허용)
	 * @param lexicalRanking 어휘 채널 순위(상위가 앞, null 허용)
	 * @param denseWeight 밀집 가중치
	 * @param lexicalWeight 어휘 가중치
	 * @param k RRF 상수(보통 {@link #DEFAULT_K})
	 * @param topK 돌려줄 상위 개수(0 이하면 빈 목록)
	 * @param <T> 융합 키 타입
	 * @return 융합 점수 내림차순 상위 topK(불변)
	 */
	public static <T> List<T> fuse(List<T> denseRanking, List<T> lexicalRanking,
			double denseWeight, double lexicalWeight, int k, int topK) {
		List<Scored<T>> scored = fuseScored(denseRanking, lexicalRanking, denseWeight, lexicalWeight, k, topK);
		List<T> keys = new ArrayList<>(scored.size());
		for (Scored<T> entry : scored) {
			keys.add(entry.key());
		}
		return List.copyOf(keys);
	}

	/**
	 * {@link #fuse(List, List, double, double, int, int)} 와 같되 융합 점수를 함께 돌려준다 — 어댑터가 프레임워크 문서의
	 * score 에 싣는 용도(점수 공식을 어댑터가 다시 쓰지 않게 한다).
	 *
	 * @return 융합 점수 내림차순 상위 topK 의 (키, 점수) 쌍(불변)
	 */
	public static <T> List<Scored<T>> fuseScored(List<T> denseRanking, List<T> lexicalRanking,
			double denseWeight, double lexicalWeight, int k, int topK) {
		if (k < 0) {
			throw new IllegalArgumentException("k 는 0 이상이어야 합니다: " + k);
		}
		Map<T, Double> scores = new LinkedHashMap<>();
		accumulate(scores, denseRanking, denseWeight, k);
		accumulate(scores, lexicalRanking, lexicalWeight, k);
		List<Map.Entry<T, Double>> entries = new ArrayList<>(scores.entrySet());
		// 안정 정렬 — 동점은 삽입 순서(등장 순서) 유지
		entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
		int limit = Math.max(topK, 0);
		List<Scored<T>> fused = new ArrayList<>(Math.min(limit, entries.size()));
		for (Map.Entry<T, Double> entry : entries) {
			if (fused.size() >= limit) {
				break;
			}
			fused.add(new Scored<>(entry.getKey(), entry.getValue()));
		}
		return List.copyOf(fused);
	}

	/**
	 * 융합 결과 한 건 — 키와 RRF 점수.
	 *
	 * @param key 융합 키
	 * @param score 융합 점수(가중 역순위 합)
	 * @param <T> 융합 키 타입
	 */
	public record Scored<T>(T key, double score) {
	}

	/** 기본 k·같은 가중치로 융합한다. */
	public static <T> List<T> fuse(List<T> denseRanking, List<T> lexicalRanking, int topK) {
		return fuse(denseRanking, lexicalRanking, 1.0, 1.0, DEFAULT_K, topK);
	}

	private static <T> void accumulate(Map<T, Double> scores, List<T> ranking, double weight, int k) {
		if (ranking == null) {
			return;
		}
		for (int rank = 0; rank < ranking.size(); rank++) {
			T key = ranking.get(rank);
			if (key == null) {
				continue;
			}
			scores.merge(key, weight / (k + rank), Double::sum);
		}
	}
}
