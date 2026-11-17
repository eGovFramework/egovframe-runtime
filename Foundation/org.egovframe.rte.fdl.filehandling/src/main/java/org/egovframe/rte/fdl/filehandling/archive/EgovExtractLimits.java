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
package org.egovframe.rte.fdl.filehandling.archive;

/**
 * 아카이브 해제 한도(엔트리 수·누적 해제 바이트) 값 객체.
 * 압축 폭탄 방지를 위해 실제 해제한 엔트리 수·기록 바이트에 적용한다. 사용자 업로드 zip은 {@link #of(long, long)}로 낮게 지정한다.
 *
 * <pre>
 * EgovArchives.unzip(uploaded, workDir, StandardCharsets.UTF_8, EgovExtractLimits.of(1_000, 100L * 1024 * 1024));
 * </pre>
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
 * @since 4.3.1
 */
public final class EgovExtractLimits {

	/** 기본 최대 엔트리 수. */
	public static final long DEFAULT_MAX_ENTRIES = 100_000L;

	/** 기본 최대 누적 해제 바이트(10 GiB). */
	public static final long DEFAULT_MAX_TOTAL_BYTES = 10L * 1024 * 1024 * 1024;

	private static final EgovExtractLimits DEFAULTS =
			new EgovExtractLimits(DEFAULT_MAX_ENTRIES, DEFAULT_MAX_TOTAL_BYTES);

	private static final EgovExtractLimits UNLIMITED =
			new EgovExtractLimits(Long.MAX_VALUE, Long.MAX_VALUE);

	private final long maxEntries;
	private final long maxTotalBytes;

	private EgovExtractLimits(long maxEntries, long maxTotalBytes) {
		this.maxEntries = maxEntries;
		this.maxTotalBytes = maxTotalBytes;
	}

	/** 기본 한도(엔트리 {@value #DEFAULT_MAX_ENTRIES}개, 누적 10 GiB). */
	public static EgovExtractLimits defaults() {
		return DEFAULTS;
	}

	/**
	 * 한도를 직접 지정한다(둘 다 양수).
	 *
	 * @param maxEntries    최대 엔트리 수
	 * @param maxTotalBytes 최대 누적 해제 바이트
	 * @throws IllegalArgumentException 0 이하 값이 있는 경우
	 */
	public static EgovExtractLimits of(long maxEntries, long maxTotalBytes) {
		if (maxEntries <= 0 || maxTotalBytes <= 0) {
			throw new IllegalArgumentException(
					"limits must be positive: maxEntries=" + maxEntries + ", maxTotalBytes=" + maxTotalBytes);
		}
		return new EgovExtractLimits(maxEntries, maxTotalBytes);
	}

	/** 한도 없음. 출처를 신뢰할 수 있는 아카이브에만 사용한다. */
	public static EgovExtractLimits unlimited() {
		return UNLIMITED;
	}

	/** 최대 엔트리 수. */
	public long getMaxEntries() {
		return maxEntries;
	}

	/** 최대 누적 해제 바이트. */
	public long getMaxTotalBytes() {
		return maxTotalBytes;
	}

}
