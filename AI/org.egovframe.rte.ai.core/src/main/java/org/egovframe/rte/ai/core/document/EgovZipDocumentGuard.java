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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * zip 기반 문서(HWPX 등)를 외부 파서에 넘기기 <b>전에</b> 검사하는 가드 — DOCTYPE(XXE)·압축 폭탄 차단.
 *
 * <p><b>왜 필요한가.</b> hwpxlib 의 XML 리더는 {@code SAXParserFactory.newInstance().newSAXParser()} 를
 * 아무 보안 기능 설정 없이 쓴다 — JDK 기본 SAX 파서는 DOCTYPE 의 외부 엔티티를 해석하므로, 본문 섹션에
 * {@code <!DOCTYPE ... [<!ENTITY x SYSTEM "file:///etc/passwd">]>} 를 넣은 HWPX 를 색인하면 서버의 로컬 파일
 * 내용이 추출 본문(그리고 벡터 저장소)에 실린다(2026-09-07 테스트로 실증). 라이브러리를 고칠 수 없으므로
 * 파싱 전에 문서 자체를 거른다: <b>DOCTYPE 선언이 든 엔트리는 거부</b>하고(OWPML 은 DOCTYPE 을 쓰지 않는다),
 * 엔트리 수·엔트리별 해제 크기·누적 해제 크기가 한도를 넘으면 <b>실제로 풀린 바이트 기준</b>으로 중단한다
 * (중앙 디렉터리의 선언 크기는 믿지 않는다).</p>
 *
 * <p>DOCTYPE 검사는 확장자와 무관하게 <b>모든 엔트리</b>에 적용한다 — 매니페스트가 가리키는 엔트리 이름을
 * 공격자가 바꿀 수 있어 이름으로 XML 여부를 판정할 수 없기 때문이다. UTF-8 외에 UTF-16 로 인코딩된
 * DOCTYPE 도 잡는다. 대가로 DOCTYPE 을 가진 구형 SVG 를 그림으로 품은 문서는 거부된다(드물며, 한도와 함께
 * 생성자로 조정할 수 없는 고정 규칙이다).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.07  실행환경팀     최초 생성 (파일 처리 보안 점검 — hwpxlib XXE 실증에 따른 파싱 전 가드)
 *  2026.09.18  실행환경팀     한도 초과를 EgovExtractionBudgetExceededException(하위 형)으로 구분 — DOCX 에도 같은 가드 적용
 *                            (R51-06 파싱 자원 예산)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovZipDocumentGuard {

	/** 기본 엔트리 수 한도. */
	public static final long DEFAULT_MAX_ENTRIES = 10_000L;

	/** 기본 엔트리별 해제 크기 한도(256 MiB). */
	public static final long DEFAULT_MAX_ENTRY_BYTES = 256L * 1024 * 1024;

	/** 기본 누적 해제 크기 한도(1 GiB). */
	public static final long DEFAULT_MAX_TOTAL_BYTES = 1024L * 1024 * 1024;

	private static final EgovZipDocumentGuard DEFAULTS =
			new EgovZipDocumentGuard(DEFAULT_MAX_ENTRIES, DEFAULT_MAX_ENTRY_BYTES, DEFAULT_MAX_TOTAL_BYTES);

	private final long maxEntries;
	private final long maxEntryBytes;
	private final long maxTotalBytes;

	private EgovZipDocumentGuard(long maxEntries, long maxEntryBytes, long maxTotalBytes) {
		this.maxEntries = maxEntries;
		this.maxEntryBytes = maxEntryBytes;
		this.maxTotalBytes = maxTotalBytes;
	}

	/** 기본 한도 — 엔트리 {@value #DEFAULT_MAX_ENTRIES}개, 엔트리별 256 MiB, 누적 1 GiB. */
	public static EgovZipDocumentGuard defaults() {
		return DEFAULTS;
	}

	/**
	 * 한도를 지정해 만든다(전부 양수).
	 *
	 * @throws IllegalArgumentException 양수가 아닌 한도
	 */
	public static EgovZipDocumentGuard of(long maxEntries, long maxEntryBytes, long maxTotalBytes) {
		if (maxEntries <= 0 || maxEntryBytes <= 0 || maxTotalBytes <= 0) {
			throw new IllegalArgumentException("limits must be positive: entries=" + maxEntries
					+ ", entryBytes=" + maxEntryBytes + ", totalBytes=" + maxTotalBytes);
		}
		return new EgovZipDocumentGuard(maxEntries, maxEntryBytes, maxTotalBytes);
	}

	public long getMaxEntries() {
		return maxEntries;
	}

	public long getMaxEntryBytes() {
		return maxEntryBytes;
	}

	public long getMaxTotalBytes() {
		return maxTotalBytes;
	}

	/**
	 * zip 문서를 검사한다 — 통과하면 그대로 반환하고, 걸리면 예외다.
	 *
	 * @param zipFile 검사할 문서
	 * @throws EgovExtractionException DOCTYPE 엔트리·한도 초과·zip 읽기 실패
	 */
	public void check(Path zipFile) {
		Objects.requireNonNull(zipFile, "zipFile must not be null");
		String fileName = String.valueOf(zipFile.getFileName());
		long entries = 0L;
		long total = 0L;
		try (ZipFile zip = new ZipFile(zipFile.toFile())) {
			Enumeration<? extends ZipEntry> names = zip.entries();
			while (names.hasMoreElements()) {
				ZipEntry entry = names.nextElement();
				if (++entries > maxEntries) {
					throw new EgovExtractionBudgetExceededException(EgovExtractionBudgetExceededException.Limit.ZIP_ENTRIES,
							maxEntries, entries, fileName, "zip 엔트리 수가 한도(" + maxEntries + ")를 넘습니다: " + fileName);
				}
				if (entry.isDirectory()) {
					continue;
				}
				total += scanEntry(zip, entry, fileName, total);
			}
		} catch (IOException e) {
			throw new EgovExtractionException("zip 문서를 읽지 못했습니다: " + fileName, e);
		}
	}

	/** 엔트리를 끝까지 풀어 읽으며(실제 해제 바이트 기준) 크기 한도와 DOCTYPE 을 검사한다. */
	private long scanEntry(ZipFile zip, ZipEntry entry, String fileName, long totalSoFar) throws IOException {
		long entryBytes = 0L;
		DoctypeScanner scanner = new DoctypeScanner();
		try (InputStream in = zip.getInputStream(entry)) {
			byte[] buffer = new byte[64 * 1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				entryBytes += read;
				if (entryBytes > maxEntryBytes || totalSoFar + entryBytes > maxTotalBytes) {
					boolean perEntry = entryBytes > maxEntryBytes;
					throw new EgovExtractionBudgetExceededException(EgovExtractionBudgetExceededException.Limit.ZIP_BYTES,
							perEntry ? maxEntryBytes : maxTotalBytes, perEntry ? entryBytes : totalSoFar + entryBytes, fileName,
							"zip 엔트리 해제 크기가 한도(엔트리 " + maxEntryBytes + "바이트, 누적 " + maxTotalBytes
									+ "바이트)를 넘습니다: " + entry.getName() + " in " + fileName);
				}
				if (scanner.feed(buffer, read)) {
					throw new EgovExtractionException("DOCTYPE 선언이 든 엔트리는 허용하지 않습니다(외부 엔티티·XXE 차단): "
							+ entry.getName() + " in " + fileName);
				}
			}
		}
		return entryBytes;
	}

	/** 청크 경계를 넘는 {@code <!DOCTYPE} 도 놓치지 않는 스트리밍 탐지기(ASCII/UTF-8·UTF-16LE·UTF-16BE, 대소문자 무시). */
	static final class DoctypeScanner {

		private static final byte[][] PATTERNS = {
				"<!DOCTYPE".getBytes(StandardCharsets.US_ASCII),
				"<!DOCTYPE".getBytes(StandardCharsets.UTF_16LE),
				"<!DOCTYPE".getBytes(StandardCharsets.UTF_16BE) };

		/** 가장 긴 패턴 길이 - 1 — 청크 경계에 걸친 패턴을 잇기 위해 앞 청크 꼬리를 이만큼 남긴다. */
		private static final int OVERLAP = 17;

		private byte[] carry = new byte[0];

		boolean feed(byte[] chunk, int length) {
			byte[] window = new byte[carry.length + length];
			System.arraycopy(carry, 0, window, 0, carry.length);
			System.arraycopy(chunk, 0, window, carry.length, length);
			if (containsDoctype(window)) {
				return true;
			}
			int keep = Math.min(OVERLAP, window.length);
			carry = Arrays.copyOfRange(window, window.length - keep, window.length);
			return false;
		}

		static boolean containsDoctype(byte[] data) {
			for (byte[] pattern : PATTERNS) {
				if (indexOf(data, pattern) >= 0) {
					return true;
				}
			}
			return false;
		}

		private static int indexOf(byte[] data, byte[] pattern) {
			outer:
			for (int i = 0; i + pattern.length <= data.length; i++) {
				for (int j = 0; j < pattern.length; j++) {
					if (upper(data[i + j]) != pattern[j]) {
						continue outer;
					}
				}
				return i;
			}
			return -1;
		}

		private static byte upper(byte b) {
			return (b >= 'a' && b <= 'z') ? (byte) (b - 32) : b;
		}
	}
}
