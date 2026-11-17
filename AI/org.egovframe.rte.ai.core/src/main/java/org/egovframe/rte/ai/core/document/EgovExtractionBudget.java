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
package org.egovframe.rte.ai.core.document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * 문서 텍스트 추출의 <b>자원 예산</b> — 파일 바이트·PDF 페이지 수·추출 문자 수·zip 컨테이너 한도를 한 값 객체로 묶는다.
 *
 * <p><b>왜 필요한가.</b> 파싱 한도가 형식마다 따로 있거나 없었다(HWPX 만 zip 가드, DOCX 는 POI 전역 설정, PDF 는 전 페이지 로드,
 * 문자 수 상한 없음 — 외부 감사 R51-06). 예산 한 벌을 추출기 모음에 주면 어떤 추출기(사용자 구현 포함)든 파일을 열기 전에 파일 크기가,
 * 추출 뒤에 문자 수가 같은 기준으로 검사되고, 내장 추출기는 형식 안쪽 한도(페이지·zip)를 같은 예산에서 받는다.</p>
 *
 * <p><b>기본 예산</b>은 파일 256 MiB · 페이지 2,000 · 문자 2,000만 · zip 가드 기본값(엔트리 10,000 · 개별 256 MiB · 누적 1 GiB)이다.
 * 현실 문서를 넘지 않으면서 병적 입력을 막는 크기이며, 초대형 문서를 다루는 자리는 {@link #builder()} 로 늘리거나 {@link #unlimited()}
 * 를 <b>명시</b>한다. 무제한도 zip 의 DOCTYPE 차단은 유지한다(가드의 고정 규칙).</p>
 *
 * <p>한도를 넘으면 {@link EgovExtractionBudgetExceededException} 이며 잘린 본문 같은 부분 결과는 돌려주지 않는다. 시간 제한·동시
 * 실행 수는 이 객체의 범위 밖이다 — 예산으로 작업량이 유한해진 뒤 호출자가 실행기로 감싼다(파서는 interrupt 를 보지 않으므로
 * 타임아웃은 "대기 중단"이지 "파싱 중단"이 아니다).</p>
 *
 * <pre>
 * EgovTextExtractors extractors = EgovTextExtractors.defaults(
 *         EgovExtractionBudget.builder().maxFileBytes(50L * 1024 * 1024).maxPages(500).build());
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.18  실행환경팀     최초 생성 (R51-06 파싱 자원 예산 · 보안점검 1부 4장 #5)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovExtractionBudget {

	/** 기본 파일 바이트 한도(256 MiB). */
	public static final long DEFAULT_MAX_FILE_BYTES = 256L * 1024 * 1024;

	/** 기본 PDF 페이지 한도. */
	public static final int DEFAULT_MAX_PAGES = 2_000;

	/** 기본 추출 문자 한도(한 파일의 결과 전체, 2,000만 자). */
	public static final long DEFAULT_MAX_CHARS = 20_000_000L;

	private static final EgovExtractionBudget DEFAULTS = new Builder().build();
	private static final EgovExtractionBudget UNLIMITED = new Builder()
			.maxFileBytes(Long.MAX_VALUE).maxPages(Integer.MAX_VALUE).maxChars(Long.MAX_VALUE)
			.zipGuard(EgovZipDocumentGuard.of(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE)).build();

	private final long maxFileBytes;
	private final int maxPages;
	private final long maxChars;
	private final EgovZipDocumentGuard zipGuard;

	private EgovExtractionBudget(Builder builder) {
		this.maxFileBytes = builder.maxFileBytes;
		this.maxPages = builder.maxPages;
		this.maxChars = builder.maxChars;
		this.zipGuard = builder.zipGuard;
	}

	/** 기본 예산. */
	public static EgovExtractionBudget defaults() {
		return DEFAULTS;
	}

	/** 무제한(명시적 선택) — 파일·페이지·문자·zip 크기 한도 없음. zip 의 DOCTYPE 차단은 유지된다. */
	public static EgovExtractionBudget unlimited() {
		return UNLIMITED;
	}

	/** 빌더(기본값에서 출발). */
	public static Builder builder() {
		return new Builder();
	}

	public long getMaxFileBytes() {
		return maxFileBytes;
	}

	public int getMaxPages() {
		return maxPages;
	}

	public long getMaxChars() {
		return maxChars;
	}

	/** zip 컨테이너 형식(HWPX·DOCX)에 적용할 가드. */
	public EgovZipDocumentGuard getZipGuard() {
		return zipGuard;
	}

	/**
	 * 파일 크기가 한도 안인지 — 파일을 열기 전에 부른다.
	 *
	 * @throws EgovExtractionBudgetExceededException 한도 초과
	 * @throws EgovExtractionException               크기를 읽지 못한 경우
	 */
	public void requireFileWithinBudget(Path file) {
		Objects.requireNonNull(file, "file must not be null");
		long size;
		try {
			size = Files.size(file);
		} catch (IOException e) {
			throw new EgovExtractionException("파일 크기를 읽지 못했습니다: " + file.getFileName(), e);
		}
		if (size > maxFileBytes) {
			throw EgovExtractionBudgetExceededException.of(EgovExtractionBudgetExceededException.Limit.FILE_BYTES,
					maxFileBytes, size, String.valueOf(file.getFileName()));
		}
	}

	/**
	 * 추출 문자 수가 한도 안인지 — 한 파일의 결과 전체에 대해 부른다.
	 *
	 * @throws EgovExtractionBudgetExceededException 한도 초과
	 */
	public void requireCharsWithinBudget(long chars, String fileName) {
		if (chars > maxChars) {
			throw EgovExtractionBudgetExceededException.of(EgovExtractionBudgetExceededException.Limit.CHARS,
					maxChars, chars, fileName);
		}
	}

	/**
	 * PDF 페이지 수가 한도 안인지 — 페이지 본문을 읽기 전에 부른다.
	 *
	 * @throws EgovExtractionBudgetExceededException 한도 초과
	 */
	public void requirePagesWithinBudget(int pages, String fileName) {
		if (pages > maxPages) {
			throw EgovExtractionBudgetExceededException.of(EgovExtractionBudgetExceededException.Limit.PAGES,
					maxPages, pages, fileName);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof EgovExtractionBudget)) {
			return false;
		}
		EgovExtractionBudget that = (EgovExtractionBudget) o;
		return maxFileBytes == that.maxFileBytes && maxPages == that.maxPages && maxChars == that.maxChars
				&& zipGuard.getMaxEntries() == that.zipGuard.getMaxEntries()
				&& zipGuard.getMaxEntryBytes() == that.zipGuard.getMaxEntryBytes()
				&& zipGuard.getMaxTotalBytes() == that.zipGuard.getMaxTotalBytes();
	}

	@Override
	public int hashCode() {
		return Objects.hash(maxFileBytes, maxPages, maxChars, zipGuard.getMaxEntries(), zipGuard.getMaxEntryBytes(),
				zipGuard.getMaxTotalBytes());
	}

	@Override
	public String toString() {
		return "EgovExtractionBudget[fileBytes=" + maxFileBytes + ", pages=" + maxPages + ", chars=" + maxChars
				+ ", zipEntries=" + zipGuard.getMaxEntries() + ", zipEntryBytes=" + zipGuard.getMaxEntryBytes()
				+ ", zipTotalBytes=" + zipGuard.getMaxTotalBytes() + "]";
	}

	/** {@link EgovExtractionBudget} 빌더 — 값은 전부 양수여야 한다. */
	public static final class Builder {
		private long maxFileBytes = DEFAULT_MAX_FILE_BYTES;
		private int maxPages = DEFAULT_MAX_PAGES;
		private long maxChars = DEFAULT_MAX_CHARS;
		private EgovZipDocumentGuard zipGuard = EgovZipDocumentGuard.defaults();

		private Builder() {
		}

		public Builder maxFileBytes(long maxFileBytes) {
			if (maxFileBytes <= 0) {
				throw new IllegalArgumentException("maxFileBytes must be positive: " + maxFileBytes);
			}
			this.maxFileBytes = maxFileBytes;
			return this;
		}

		public Builder maxPages(int maxPages) {
			if (maxPages <= 0) {
				throw new IllegalArgumentException("maxPages must be positive: " + maxPages);
			}
			this.maxPages = maxPages;
			return this;
		}

		public Builder maxChars(long maxChars) {
			if (maxChars <= 0) {
				throw new IllegalArgumentException("maxChars must be positive: " + maxChars);
			}
			this.maxChars = maxChars;
			return this;
		}

		public Builder zipGuard(EgovZipDocumentGuard zipGuard) {
			this.zipGuard = Objects.requireNonNull(zipGuard, "zipGuard must not be null");
			return this;
		}

		public EgovExtractionBudget build() {
			return new EgovExtractionBudget(this);
		}
	}
}
