package org.egovframe.rte.ai.core.retrieval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RRF 융합 계약 테스트 — 수식·동점 결정성·가중치·상위 N·null 채널(스펙 ai/hybrid-retrieval 1항). 참고 샘플 케이스 이식.
 */
class EgovRrfFusionTest {

	private static final int K = EgovRrfFusion.DEFAULT_K;

	@Test
	@DisplayName("표준 RRF 수식(k=60)에 따라 융합되고 동점은 등장 순서다")
	void standardFormulaAndTies() {
		assertEquals(List.of("A", "B", "C", "D"),
				EgovRrfFusion.fuse(List.of("A", "B", "C"), List.of("B", "A", "D"), 1.0, 1.0, K, 10));
	}

	@Test
	@DisplayName("양 채널 상위에 모두 등장한 키가 최상위다")
	void itemInBothChannelsRanksHighest() {
		List<String> fused = EgovRrfFusion.fuse(List.of("X", "A", "B"), List.of("X", "C", "D"), 1.0, 1.0, K, 5);
		assertEquals("X", fused.get(0));
		assertEquals(5, fused.size());
		assertEquals("c", EgovRrfFusion.fuse(List.of("a", "b", "c"), List.of("c", "d"), 3).get(0));
	}

	@Test
	@DisplayName("어휘 가중치를 키우면 어휘 채널 상위 키가 우선된다")
	void lexicalWeightShiftsOrder() {
		assertEquals(List.of("D", "L"), EgovRrfFusion.fuse(List.of("D"), List.of("L"), 1.0, 1.0, K, 2));
		assertEquals(List.of("L", "D"), EgovRrfFusion.fuse(List.of("D"), List.of("L"), 1.0, 3.0, K, 2));
	}

	@Test
	@DisplayName("상위 N 만 돌려주고 한쪽 채널만 있어도, null 채널이어도 융합된다")
	void topKAndSingleOrNullChannel() {
		assertEquals(3, EgovRrfFusion.fuse(List.of("A", "B", "C", "D", "E"), List.of("F", "G"), 1.0, 1.0, K, 3).size());
		assertEquals(List.of("A", "B"), EgovRrfFusion.fuse(List.of("A", "B"), List.of(), 1.0, 1.0, K, 5));
		assertEquals(List.of("A", "B"), EgovRrfFusion.fuse(List.of("A", "B"), null, 1.0, 1.0, K, 5));
		assertEquals(List.of(), EgovRrfFusion.fuse(List.of("A"), List.of("B"), 1.0, 1.0, K, 0));
	}

	@Test
	@DisplayName("양 채널 중위권 정답이 융합으로 상위에 오른다(recall@2 0.5 → 1.0)")
	void crossChannelReinforcement() {
		List<String> fused = EgovRrfFusion.fuse(List.of("n1", "REL1", "REL2", "n2"), List.of("m1", "REL2", "REL1", "m2"), 1.0, 1.0, K, 2);
		assertEquals(2, fused.size());
		assertEquals(true, fused.contains("REL1") && fused.contains("REL2"));
	}

	@Test
	@DisplayName("null 키는 무시되고 음수 k 는 거부된다")
	void nullKeysAndNegativeK() {
		assertEquals(List.of("A"), EgovRrfFusion.fuse(java.util.Arrays.asList("A", null), List.of(), 1.0, 1.0, K, 5));
		assertThrows(IllegalArgumentException.class, () -> EgovRrfFusion.fuse(List.of("A"), List.of(), 1.0, 1.0, -1, 5));
	}

	@Test
	@DisplayName("fuseScored 는 같은 순서에 RRF 점수를 함께 돌려준다")
	void fuseScoredCarriesScores() {
		List<EgovRrfFusion.Scored<String>> scored = EgovRrfFusion.fuseScored(List.of("A", "B", "C"), List.of("C", "D"), 1.0, 1.0, 60, 4);
		assertEquals(List.of("C", "A", "B", "D"), scored.stream().map(EgovRrfFusion.Scored::key).toList());
		assertEquals(1.0 / 62 + 1.0 / 60, scored.get(0).score(), 1e-12, "양 채널 합");
		assertEquals(1.0 / 60, scored.get(1).score(), 1e-12);
		for (int i = 1; i < scored.size(); i++) {
			org.junit.jupiter.api.Assertions.assertTrue(scored.get(i - 1).score() >= scored.get(i).score(), "내림차순");
		}
	}
}
