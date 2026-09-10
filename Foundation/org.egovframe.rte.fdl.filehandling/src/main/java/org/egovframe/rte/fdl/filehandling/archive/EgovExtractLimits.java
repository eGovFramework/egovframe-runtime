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
 * 아카이브 해제 한도 — 압축 폭탄(zip bomb)형 자원 고갈을 봉쇄하는 상한 값.
 *
 * <p><b>NOTE:</b> 아카이브의 엔트리 수·압축 해제 후 크기는 <b>파일 안에 적힌 값을 믿을 수 없다</b>
 * (조작 가능). {@link EgovArchives}는 이 한도를 실제로 만든 엔트리 수·실제로 쓴 바이트 수에
 * 적용한다. 기본값({@link #defaults()})은 일반 업무 아카이브가 걸리지 않는 수준의 안전망이며,
 * 사용자 업로드 zip 해제처럼 신뢰 경계를 넘는 자리는 {@link #of(long, long)}로 훨씬 낮게 잡고,
 * 운영자가 직접 만든 대용량 백업 복원은 {@link #unlimited()}를 명시해 푼다.</p>
 *
 * <pre>
 * EgovArchives.unzip(uploaded, workDir, StandardCharsets.UTF_8,
 *         EgovExtractLimits.of(1_000, 100L * 1024 * 1024));   // 1,000개·100MB 상한
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (원본 EgovFileCmprs 에 없던 해제 한도 신설)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
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

	/**
	 * 기본 한도 — 엔트리 {@value #DEFAULT_MAX_ENTRIES}개, 누적 10 GiB.
	 */
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

	/**
	 * 한도 없음 — 운영자가 직접 만든 대용량 백업 복원처럼 <b>출처를 신뢰할 수 있는 경우에만</b> 쓴다.
	 */
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
