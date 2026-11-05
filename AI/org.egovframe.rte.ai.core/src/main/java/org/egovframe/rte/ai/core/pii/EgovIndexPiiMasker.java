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
package org.egovframe.rte.ai.core.pii;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 색인 단계 PII 마스커 — 유출 위험이 크고 검색 기여가 낮은 식별자만 토큰으로 치환한다.
 *
 * <p>대상: 주민등록번호(외국인등록번호 포함)·카드번호·인증키(키=값)·운영자 정규식 계좌번호. 전화번호·이메일·이름·주소는
 * 업무 검색에 필요하므로 대상이 아니다. 오탐 억제 — 주민등록번호는 {@link EgovRrnRules} 값 규칙(기본 형식 검증 → 2020-10
 * 이후 신형 수용, 선택 시 레거시 검증번호)을 통과한 후보만, 카드번호는 13~19자리 후보 중 Luhn 통과만(주민번호 형태 13자리는
 * 카드로 보지 않음), 인증키는 값이 자격증명처럼 보일 때만(6자 이상·한글 없음·숫자/기호 포함). 결과는 치환 텍스트와 유형별
 * <b>건수만</b>이며 값·표본은 어디에도 남지 않는다.</p>
 *
 * <p>참고 샘플은 주민등록번호 체크섬을 재구현해 신형 번호를 놓쳤고 이를 주석으로 자인했다 — 같은 패키지의
 * {@link EgovRrnRules} 값 규칙(형식 검증 / 레거시 검증번호)에 위임해 교정했다.</p>
 *
 * <pre>
 * EgovIndexPiiMasker masker = EgovIndexPiiMasker.builder().build();          // 주민·카드·인증키 기본 활성
 * EgovIndexPiiMasker.Result result = masker.mask(text);
 * EgovAiAudit.masking(result.counts());                                       // 어댑터가 건수만 감사 발신
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플 EgovPiiMaskingTransformer 설계 차용, 값 규칙 위임)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovIndexPiiMasker {

	/** 마스킹 유형. */
	public enum Kind {
		/** 주민등록번호·외국인등록번호. */
		RRN,
		/** 카드번호. */
		CARD,
		/** 인증키·비밀키 값. */
		SECRET,
		/** 계좌번호(운영자 정규식). */
		ACCOUNT
	}

	/** 주민등록번호 값 검사 모드. */
	public enum ResidentNoCheck {
		/** 형식 검증(기본) — 2020-10 이후 신형 포함 전 세대 안전. */
		FORMAT,
		/** 레거시 검증번호(opt-in) — 2020-10 이전 부여분만 존재함이 보장된 데이터 정제 전용. */
		LEGACY_CHECKSUM
	}

	/** 마스킹 결과 — 치환 텍스트와 유형별 건수(값 없음). */
	public static final class Result {
		private final String text;
		private final Map<Kind, Integer> counts;

		private Result(String text, Map<Kind, Integer> counts) {
			this.text = text;
			this.counts = Collections.unmodifiableMap(counts);
		}

		/** 치환된 텍스트(치환 없으면 입력과 같은 객체). */
		public String text() {
			return text;
		}

		/** 유형별 치환 건수(4유형 전부 포함, 없으면 0). */
		public Map<Kind, Integer> counts() {
			return counts;
		}

		/** 총 치환 건수. */
		public int total() {
			int total = 0;
			for (int count : counts.values()) {
				total += count;
			}
			return total;
		}

		/** 치환이 있었는가. */
		public boolean changed() {
			return total() > 0;
		}
	}

	/** 주민등록번호 후보 — 하이픈·공백·무구분 표기(앞 6·성별 1~8·뒤 6), 숫자에 둘러싸이지 않은 것. */
	static final Pattern RRN_CANDIDATE = Pattern.compile("(?<![0-9])(\\d{6})[- ]?([1-8]\\d{6})(?![0-9])");

	/** 카드번호 후보 — 13~19자리(자리 사이에만 공백·하이픈). Luhn 으로 최종 확정. */
	static final Pattern CARD_CANDIDATE = Pattern.compile("(?<![0-9])\\d(?:[ -]?\\d){12,18}(?![0-9])");

	/**
	 * 인증키·비밀키 — 키=값·키: 값 형태의 값만. {@code Authorization: Bearer <토큰>} 처럼 인증 스킴 단어가 앞에 오면
	 * 스킴을 건너뛰고 실제 토큰을 값으로 본다(스킴 단어만 보고 "비밀 아님" 으로 넘기던 미탐 교정 — 2026-09-07 보안 점검).
	 */
	static final Pattern SECRET = Pattern.compile(
			"(?i)\\b(api[_-]?key|secret(?:[_-]?key)?|access[_-]?key|private[_-]?key|"
					+ "password|passwd|pwd|token|authorization)\\b\\s*[:=]\\s*[\"']?"
					+ "(?:(?:bearer|basic|token|digest)\\s+)?([^\\s\"',]+)");

	private static final Pattern HANGUL = Pattern.compile("[가-힣ㄱ-ㅎㅏ-ㅣ]");
	private static final Pattern NON_LETTER = Pattern.compile("[^A-Za-z]");
	private static final int MIN_SECRET_LENGTH = 6;

	private final boolean residentNo;
	private final ResidentNoCheck residentNoCheck;
	private final boolean cardNo;
	private final boolean secret;
	private final Pattern accountPattern;
	private final String residentNoToken;
	private final String cardNoToken;
	private final String secretToken;
	private final String accountToken;

	private EgovIndexPiiMasker(Builder builder) {
		this.residentNo = builder.residentNo;
		this.residentNoCheck = builder.residentNoCheck;
		this.cardNo = builder.cardNo;
		this.secret = builder.secret;
		this.accountPattern = builder.accountPattern;
		this.residentNoToken = builder.residentNoToken;
		this.cardNoToken = builder.cardNoToken;
		this.secretToken = builder.secretToken;
		this.accountToken = builder.accountToken;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 마스킹한다. null·빈 입력은 그대로(건수 0).
	 */
	public Result mask(String text) {
		Map<Kind, Integer> counts = new EnumMap<>(Kind.class);
		for (Kind kind : Kind.values()) {
			counts.put(kind, 0);
		}
		if (text == null || text.isEmpty()) {
			return new Result(text, counts);
		}
		String result = text;
		if (residentNo) {
			result = maskResidentNos(result, counts);
		}
		if (cardNo) {
			result = maskCardNos(result, counts);
		}
		if (accountPattern != null) {
			Matcher matcher = accountPattern.matcher(result);
			StringBuilder sb = new StringBuilder();
			int n = 0;
			while (matcher.find()) {
				matcher.appendReplacement(sb, Matcher.quoteReplacement(accountToken));
				n++;
			}
			matcher.appendTail(sb);
			result = sb.toString();
			counts.merge(Kind.ACCOUNT, n, Integer::sum);
		}
		if (secret) {
			result = maskSecrets(result, counts);
		}
		return new Result(result.equals(text) ? text : result, counts);
	}

	/** 값 규칙을 통과한 후보만 치환한다. */
	private String maskResidentNos(String text, Map<Kind, Integer> counts) {
		Matcher matcher = RRN_CANDIDATE.matcher(text);
		StringBuilder sb = new StringBuilder();
		int n = 0;
		while (matcher.find()) {
			String digits = matcher.group(1) + matcher.group(2);
			boolean valid = residentNoCheck == ResidentNoCheck.LEGACY_CHECKSUM
					? EgovRrnRules.isValidLegacyChecksum(digits)
					: EgovRrnRules.isValidFormat(digits);
			String replacement = valid ? residentNoToken : matcher.group();
			if (valid) {
				n++;
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		counts.merge(Kind.RRN, n, Integer::sum);
		return sb.toString();
	}

	/**
	 * Luhn 통과 후보만 치환한다. 주민등록번호 형태의 13자리는 카드로 취급하지 않는다 — 이 단계는 주민번호 마스킹 뒤에
	 * 실행되므로 남아 있는 주민번호 형태는 값 규칙을 통과하지 못한 관리코드·기안번호 등이며 카드로 재분류하면 다시 오탐이다.
	 */
	private String maskCardNos(String text, Map<Kind, Integer> counts) {
		Matcher matcher = CARD_CANDIDATE.matcher(text);
		StringBuilder sb = new StringBuilder();
		int n = 0;
		while (matcher.find()) {
			String candidate = matcher.group();
			String replacement = candidate;
			if (!RRN_CANDIDATE.matcher(candidate).matches()) {
				String digits = candidate.replaceAll("[ -]", "");
				if (digits.length() >= 13 && digits.length() <= 19 && EgovLuhn.isValid(digits)) {
					replacement = cardNoToken;
					n++;
				}
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		counts.merge(Kind.CARD, n, Integer::sum);
		return sb.toString();
	}

	/** 키 이름은 보존하고 자격증명처럼 보이는 값만 치환한다. */
	private String maskSecrets(String text, Map<Kind, Integer> counts) {
		Matcher matcher = SECRET.matcher(text);
		StringBuilder sb = new StringBuilder();
		int n = 0;
		while (matcher.find()) {
			String replacement;
			if (looksLikeSecret(matcher.group(2))) {
				replacement = text.substring(matcher.start(), matcher.start(2)) + secretToken;
				n++;
			} else {
				replacement = matcher.group();
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		counts.merge(Kind.SECRET, n, Integer::sum);
		return sb.toString();
	}

	/** 6자 이상·한글 없음·숫자/기호 포함 — 안내문("8자 이상으로 설정")·짧은 단어(JWT)·순수 영문 단어(production)를 배제. */
	private static boolean looksLikeSecret(String value) {
		return value.length() >= MIN_SECRET_LENGTH
				&& !HANGUL.matcher(value).find()
				&& NON_LETTER.matcher(value).find();
	}

	/** {@link EgovIndexPiiMasker} 빌더 — 주민·카드·인증키 활성, 계좌 비활성(정규식 미지정)이 기본. */
	public static final class Builder {
		private boolean residentNo = true;
		private ResidentNoCheck residentNoCheck = ResidentNoCheck.FORMAT;
		private boolean cardNo = true;
		private boolean secret = true;
		private Pattern accountPattern;
		private String residentNoToken = "[RRN]";
		private String cardNoToken = "[CARD]";
		private String secretToken = "[SECRET]";
		private String accountToken = "[ACCOUNT]";

		private Builder() {
		}

		public Builder residentNo(boolean enabled) {
			this.residentNo = enabled;
			return this;
		}

		public Builder residentNoCheck(ResidentNoCheck check) {
			this.residentNoCheck = Objects.requireNonNull(check, "check must not be null");
			return this;
		}

		public Builder cardNo(boolean enabled) {
			this.cardNo = enabled;
			return this;
		}

		public Builder secret(boolean enabled) {
			this.secret = enabled;
			return this;
		}

		/** 계좌번호 정규식(null·미지정이면 계좌 규칙 비활성 — 기관별 형식이 달라 내장 패턴은 오탐이 크다). */
		public Builder accountPattern(Pattern pattern) {
			this.accountPattern = pattern;
			return this;
		}

		public Builder residentNoToken(String token) {
			this.residentNoToken = requireToken(token);
			return this;
		}

		public Builder cardNoToken(String token) {
			this.cardNoToken = requireToken(token);
			return this;
		}

		public Builder secretToken(String token) {
			this.secretToken = requireToken(token);
			return this;
		}

		public Builder accountToken(String token) {
			this.accountToken = requireToken(token);
			return this;
		}

		public EgovIndexPiiMasker build() {
			return new EgovIndexPiiMasker(this);
		}

		private static String requireToken(String token) {
			if (token == null || token.isEmpty()) {
				throw new IllegalArgumentException("치환 토큰은 비어 있을 수 없습니다");
			}
			return token;
		}
	}
}
