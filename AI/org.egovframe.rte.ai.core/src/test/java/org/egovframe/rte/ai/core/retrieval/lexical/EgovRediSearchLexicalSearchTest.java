package org.egovframe.rte.ai.core.retrieval.lexical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RediSearch 백엔드 계약 테스트 — 질의 빌더(순수)와 3단 폴백·접두어 제거·오류 전파(가짜 실행부)
 * (스펙 ai/hybrid-retrieval 2~3항). Redis 서버 없이 동작한다.
 */
class EgovRediSearchLexicalSearchTest {

	@Test
	@DisplayName("어절 분해 — 순서 보존·중복 제거·빈 질의는 빈 목록")
	void tokenize() {
		assertEquals(List.of("주민등록번호", "변경", "신청"), EgovRediSearchQueries.tokenize("주민등록번호 변경 신청"));
		assertEquals(List.of("신청", "변경"), EgovRediSearchQueries.tokenize("신청   변경 신청"));
		assertEquals(List.of(), EgovRediSearchQueries.tokenize(null));
		assertEquals(List.of(), EgovRediSearchQueries.tokenize("   "));
		// 어절 수 상한 — 수만 개 어절이 중위(*t*) OR 질의 하나로 조립돼 RediSearch 를 잠그지 못하게 한다(CWE-400, 2026-09-07 4차 점검)
		String flood = java.util.stream.IntStream.range(0, 500).mapToObj(i -> "t" + i).collect(java.util.stream.Collectors.joining(" "));
		assertTrue(EgovRediSearchQueries.tokenize(flood).size() <= 32, "어절 수는 32 이내로 잘라야 한다");
		assertEquals("t0", EgovRediSearchQueries.tokenize(flood).get(0), "앞쪽 어절을 남긴다");
	}

	@Test
	@DisplayName("정확·접두·중위 3단 질의 문자열")
	void stagedQueries() {
		EgovRediSearchQueries queries = EgovRediSearchQueries.DEFAULT;
		assertEquals("@content:(주민등록번호 변경)", queries.exactQuery(List.of("주민등록번호", "변경")));
		assertEquals("@content:(주민* | 여권*)", queries.prefixQuery(List.of("주민", "여권")));
		assertEquals("@content:(*등록* | *민원*)", queries.infixQuery(List.of("등록", "민원")));
		assertEquals(List.of("@content:(전자정부 페이징)", "@content:(전자정부* | 페이징*)", "@content:(*전자정부* | *페이징*)"),
				queries.stagedQueries("전자정부 페이징"));
		assertEquals(List.of(), queries.stagedQueries(""));
		assertEquals("@text:(a)", new EgovRediSearchQueries("text").exactQuery(List.of("a")));
	}

	@Test
	@DisplayName("RediSearch 특수문자는 이스케이프되고 접두어는 벗겨진다")
	void escapeAndPrefix() {
		assertEquals("a\\:b\\(c\\)", EgovRediSearchQueries.escape("a:b(c)"));
		assertEquals("doc\\-1\\.md", EgovRediSearchQueries.escape("doc-1.md"));
		assertEquals("주민등록", EgovRediSearchQueries.escape("주민등록"));
		assertEquals("doc-1", EgovRediSearchQueries.stripPrefix("embedding:doc-1", "embedding:"));
		assertEquals("other:doc-1", EgovRediSearchQueries.stripPrefix("other:doc-1", "embedding:"));
	}

	@Test
	@DisplayName("회수가 생긴 첫 단계를 채택하고 키 접두어를 벗긴다")
	void stagedFallbackAndPrefixStripping() {
		List<String> executed = new ArrayList<>();
		EgovRediSearchLexicalSearch.Executor executor = (query, topK) -> {
			executed.add(query);
			return query.contains("*") ? List.of("embedding:doc-2", "embedding:doc-7") : List.of();
		};
		EgovRediSearchLexicalSearch search = new EgovRediSearchLexicalSearch(executor,
				EgovRediSearchLexicalSearch.DEFAULT_KEY_PREFIX, EgovRediSearchQueries.DEFAULT);
		assertEquals(List.of("doc-2", "doc-7"), search.search("주민등록 변경", 5));
		assertEquals(List.of("@content:(주민등록 변경)", "@content:(주민등록* | 변경*)"), executed, "정확 실패 → 접두 채택, 중위 미실행");
	}

	@Test
	@DisplayName("매칭이 없으면 빈 목록, 상한 0 이면 실행하지 않는다")
	void noMatchAndZeroLimit() {
		List<String> executed = new ArrayList<>();
		EgovRediSearchLexicalSearch search = new EgovRediSearchLexicalSearch((query, topK) -> {
			executed.add(query);
			return List.of();
		}, "embedding:", EgovRediSearchQueries.DEFAULT);
		assertEquals(List.of(), search.search("없는 단어", 3));
		assertEquals(3, executed.size(), "3단 모두 시도");
		executed.clear();
		assertEquals(List.of(), search.search("무엇", 0));
		assertTrue(executed.isEmpty());
	}

	@Test
	@DisplayName("백엔드 오류는 예외로 전파된다(빈 결과로 감추지 않는다)")
	void backendErrorPropagates() {
		EgovRediSearchLexicalSearch search = new EgovRediSearchLexicalSearch((query, topK) -> {
			throw new IllegalStateException("connection refused");
		}, "embedding:", EgovRediSearchQueries.DEFAULT);
		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> search.search("주민등록", 3));
		assertEquals("connection refused", ex.getMessage());
	}
}
