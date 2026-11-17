package org.egovframe.rte.ai.core.pii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.egovframe.rte.ai.core.pii.EgovIndexPiiMasker.Kind;
import org.egovframe.rte.ai.core.pii.EgovIndexPiiMasker.ResidentNoCheck;
import org.egovframe.rte.ai.core.pii.EgovIndexPiiMasker.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 색인 마스킹 계약 테스트 — 선택적 대상·값 규칙 위임(신형 주민번호 수용 — 샘플이 놓치던 것의 회귀 고정)·Luhn·비밀 오탐 억제·
 * 건수 보고·전화/이메일 보존(스펙 ai/index-pii-masking). 모든 번호는 합성한 가상 번호다.
 */
class EgovIndexPiiMaskerTest {

	/** 앞 12자리에 구 체계 검증번호를 붙인다(내국인 표준/외국인 +2 보정). */
	private static String withLegacyChecksum(String first12) {
		int[] weights = { 2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5 };
		int sum = 0;
		for (int i = 0; i < 12; i++) {
			sum += weights[i] * (first12.charAt(i) - '0');
		}
		int check = (11 - sum % 11) % 10;
		int genderCode = first12.charAt(6) - '0';
		if (genderCode >= 5 && genderCode <= 8) {
			check = (check + 2) % 10;
		}
		return first12 + check;
	}

	private static final EgovIndexPiiMasker DEFAULT = EgovIndexPiiMasker.builder().build();

	@Test
	@DisplayName("형식 검증 모드(기본)는 2020-10 이후 신형(구 검증번호 불일치) 주민등록번호도 마스킹한다")
	void masksNewStyleResidentNo() {
		String legacyValid = withLegacyChecksum("900101123456");
		char last = legacyValid.charAt(12);
		String newStyle = legacyValid.substring(0, 12) + ((last - '0' + 1) % 10); // 검증번호 불일치 = 신형 형태
		Result result = DEFAULT.mask("문의자 " + newStyle.substring(0, 6) + "-" + newStyle.substring(6) + " 확인");
		assertEquals("문의자 [RRN] 확인", result.text());
		assertEquals(1, result.counts().get(Kind.RRN));
		assertEquals("구분자 없는 [RRN] 도", DEFAULT.mask("구분자 없는 " + legacyValid + " 도").text());
		assertEquals("공백 구분 [RRN] 도", DEFAULT.mask("공백 구분 " + legacyValid.substring(0, 6) + " " + legacyValid.substring(6) + " 도").text());
	}

	@Test
	@DisplayName("레거시 검증번호 모드는 구 공식을 통과한 번호만 마스킹한다")
	void legacyChecksumMode() {
		EgovIndexPiiMasker legacy = EgovIndexPiiMasker.builder().residentNoCheck(ResidentNoCheck.LEGACY_CHECKSUM).build();
		String valid = withLegacyChecksum("900101123456");
		assertEquals("[RRN]", legacy.mask(valid).text());
		assertEquals("850302-1234567", legacy.mask("850302-1234567").text(), "검증번호 불일치 → 레거시 모드에서는 유지");
	}

	@Test
	@DisplayName("형태만 같은 관리번호(실존하지 않는 날짜)는 어느 모드에서도 마스킹하지 않고 카드로도 보지 않는다")
	void keepsInvalidDateNumbers() {
		assertEquals("관리코드 851302-1234567 참조", DEFAULT.mask("관리코드 851302-1234567 참조").text());
		assertEquals(0, DEFAULT.mask("관리코드 851302-1234567 참조").total());
	}

	@Test
	@DisplayName("카드번호는 Luhn 통과만 마스킹한다")
	void masksOnlyLuhnValidCards() {
		assertEquals("카드 [CARD] 결제", DEFAULT.mask("카드 4111-1111-1111-1111 결제").text());
		assertEquals("카드 4111 1111 1111 1111 결제", DEFAULT.mask("카드 4111 1111 1111 1111 결제").text().replace("[CARD]", "4111 1111 1111 1111"));
		assertTrue(DEFAULT.mask("카드 4111-1111-1111-1112 결제").text().contains("4111-1111-1111-1112"));
		assertTrue(EgovLuhn.isValid("79927398713"));
		assertFalse(EgovLuhn.isValid("79927398710"));
		assertFalse(EgovLuhn.isValid("4111-1111"));
		assertFalse(EgovLuhn.isValid(null));
	}

	@Test
	@DisplayName("인증키는 값만 치환하고 키 이름은 보존하며 안내문·짧은 단어·순수 영문 단어는 오탐하지 않는다")
	void secrets() {
		assertEquals("api_key=[SECRET]", DEFAULT.mask("api_key=AKIA1234567890").text());
		assertEquals("password: [SECRET]", DEFAULT.mask("password: s3cr3tP@ss").text());
		assertEquals("pwd=[SECRET]", DEFAULT.mask("pwd=abc123").text());
		for (String prose : new String[] { "password: 대소문자를 포함한 8자 이상으로 설정하세요", "api_key: 개발자 포털에서 신청 후 발급",
				"token: JWT 형식으로 발급됩니다", "token: production 환경에 배포" }) {
			assertEquals(prose, DEFAULT.mask(prose).text());
		}
	}

	@Test
	@DisplayName("Authorization: Bearer/Basic 뒤의 실제 토큰을 마스킹한다 — 스킴 단어만 보고 놓치면 색인에 토큰이 남는다(2026-09-07 보안 점검)")
	void bearerTokensAreMasked() {
		assertEquals("Authorization: Bearer [SECRET]", DEFAULT.mask("Authorization: Bearer sk-live-abcd1234efgh").text());
		assertEquals("authorization=Basic [SECRET]", DEFAULT.mask("authorization=Basic dXNlcjpwYXNzd29yZA==").text());
		assertEquals("token: Bearer [SECRET]", DEFAULT.mask("token: Bearer eyJhbGciOi.JIUzI1NiJ9.x-y_z").text());
		assertEquals(1, DEFAULT.mask("Authorization: Bearer sk-live-abcd1234efgh").counts().get(Kind.SECRET));
	}

	@Test
	@DisplayName("인증키 정규식은 적대 입력(키워드 뒤 긴 공백·긴 값·구분자 반복)에서도 선형이다(ReDoS, 2026-09-07 4차 점검)")
	void secretPatternIsLinear() {
		String hostile = "password" + " ".repeat(200_000) + "x token=" + "a".repeat(200_000) + " authorization: " + "=".repeat(100_000);
		org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(2), () -> DEFAULT.mask(hostile));
	}

	@Test
	@DisplayName("전화번호·이메일·이름은 마스킹하지 않는다(업무 검색 보존)")
	void keepsPhoneAndEmail() {
		String text = "담당 홍길동 044-000-0000, hong@korea.kr 로 문의";
		Result result = DEFAULT.mask(text);
		assertSame(text, result.text());
		assertFalse(result.changed());
	}

	@Test
	@DisplayName("계좌 규칙은 정규식을 지정했을 때만 동작하고 규칙은 개별로 끌 수 있다")
	void accountRegexAndToggles() {
		assertTrue(DEFAULT.mask("계좌 123-456-789012").text().contains("123-456-789012"));
		EgovIndexPiiMasker account = EgovIndexPiiMasker.builder().residentNo(false).cardNo(false).secret(false)
				.accountPattern(Pattern.compile("\\b\\d{3}-\\d{3}-\\d{6}\\b")).build();
		assertEquals("계좌 [ACCOUNT]", account.mask("계좌 123-456-789012").text());
		assertEquals(1, account.mask("계좌 123-456-789012").counts().get(Kind.ACCOUNT));
		EgovIndexPiiMasker noCard = EgovIndexPiiMasker.builder().cardNo(false).build();
		assertTrue(noCard.mask("카드 4111-1111-1111-1111").text().contains("4111-1111-1111-1111"));
	}

	@Test
	@DisplayName("결과는 유형별 건수와 총계를 담고 값·표본은 없다, 치환 없으면 같은 객체, null·빈 입력 안전")
	void countsAndSafety() {
		String valid = withLegacyChecksum("900101123456");
		Result result = DEFAULT.mask("주민 " + valid + " 및 " + valid.substring(0, 6) + "-" + valid.substring(6) + " 카드 4111-1111-1111-1111");
		assertEquals(2, result.counts().get(Kind.RRN));
		assertEquals(1, result.counts().get(Kind.CARD));
		assertEquals(0, result.counts().get(Kind.SECRET));
		assertEquals(3, result.total());
		assertTrue(result.changed());
		assertFalse(result.text().contains(valid));
		assertEquals("주민 [RRN] 및 [RRN] 카드 [CARD]", result.text());
		String clean = "민감정보 없는 일반 문서";
		assertSame(clean, DEFAULT.mask(clean).text());
		assertNull(DEFAULT.mask(null).text());
		assertEquals("", DEFAULT.mask("").text());
		assertEquals(0, DEFAULT.mask(null).total());
	}

	@Test
	@DisplayName("치환 토큰은 바꿀 수 있고 비어 있으면 거부된다")
	void tokens() {
		EgovIndexPiiMasker custom = EgovIndexPiiMasker.builder().cardNoToken("<카드>").build();
		assertEquals("카드 <카드> 결제", custom.mask("카드 4111-1111-1111-1111 결제").text());
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> EgovIndexPiiMasker.builder().secretToken(""));
	}
}
