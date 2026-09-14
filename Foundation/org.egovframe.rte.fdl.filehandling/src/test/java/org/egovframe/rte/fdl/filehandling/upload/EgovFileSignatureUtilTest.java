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
package org.egovframe.rte.fdl.filehandling.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovFileSignatureUtil} 단위 테스트.
 *
 * <p>확장자 위장 탐지(핵심 목적)와 함께 <b>계약의 경계</b>를 고정한다 — 미등록 확장자의 통과(fail-open),
 * 컨테이너가 같은 포맷끼리 구분되지 않는 한계, 경로가 섞인 파일명의 확장자 판정.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.14  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovFileSignatureUtilTest {

	private static final byte[] PDF = { 0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x37 };
	private static final byte[] JPG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46 };
	private static final byte[] PNG = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
	private static final byte[] GIF = { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00 };
	private static final byte[] BMP = { 0x42, 0x4D, 0x36, 0x00, 0x00, 0x00, 0x00, 0x00 };
	private static final byte[] ZIP = { 0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x00, 0x00 };
	private static final byte[] CFBF = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A,
			(byte) 0xE1 };

	/** 확장자만 이미지로 바꿔 올리는 전형적인 위장 — 내용은 스크립트 텍스트다. */
	private static final byte[] SCRIPT = "<%@ page import=\"java.io.*\" %>".getBytes(StandardCharsets.UTF_8);

	@Nested
	@DisplayName("확장자 위장 탐지 — 이 유틸의 목적")
	class 위장_탐지 {

		@Test
		@DisplayName("스크립트 내용을 이미지 확장자로 올리면 거부한다")
		void 스크립트를_이미지로_위장() {
			assertFalse(EgovFileSignatureUtil.matches(SCRIPT, "evil.jpg"));
			assertFalse(EgovFileSignatureUtil.matches(SCRIPT, "evil.png"));
			assertFalse(EgovFileSignatureUtil.matches(SCRIPT, "evil.pdf"));
			assertFalse(EgovFileSignatureUtil.matches(SCRIPT, "evil.hwp"));
		}

		@Test
		@DisplayName("포맷은 맞지만 확장자가 다른 경우도 거부한다")
		void 다른_포맷의_확장자() {
			assertFalse(EgovFileSignatureUtil.matches(PNG, "actually-png.jpg"));
			assertFalse(EgovFileSignatureUtil.matches(ZIP, "actually-zip.pdf"));
			assertFalse(EgovFileSignatureUtil.matches(CFBF, "actually-hwp.hwpx"));
		}

		@Test
		@DisplayName("내용이 시그니처보다 짧으면 거부한다")
		void 너무_짧은_내용() {
			assertFalse(EgovFileSignatureUtil.matches(new byte[] { (byte) 0xFF }, "tiny.jpg"));
			assertFalse(EgovFileSignatureUtil.matches(new byte[0], "empty.png"));
		}
	}

	@Nested
	@DisplayName("정상 파일은 통과시킨다")
	class 정상_통과 {

		@Test
		@DisplayName("등록된 포맷의 내용과 확장자가 맞으면 통과한다")
		void 포맷별_일치() {
			assertTrue(EgovFileSignatureUtil.matches(PDF, "report.pdf"));
			assertTrue(EgovFileSignatureUtil.matches(JPG, "photo.jpg"));
			assertTrue(EgovFileSignatureUtil.matches(JPG, "photo.jpeg"));
			assertTrue(EgovFileSignatureUtil.matches(PNG, "logo.png"));
			assertTrue(EgovFileSignatureUtil.matches(GIF, "anim.gif"));
			assertTrue(EgovFileSignatureUtil.matches(BMP, "raster.bmp"));
			assertTrue(EgovFileSignatureUtil.matches(ZIP, "archive.zip"));
			assertTrue(EgovFileSignatureUtil.matches(CFBF, "old.hwp"));
		}

		@Test
		@DisplayName("확장자 대소문자를 가리지 않는다")
		void 대소문자_무관() {
			assertTrue(EgovFileSignatureUtil.matches(JPG, "PHOTO.JPG"));
			assertTrue(EgovFileSignatureUtil.matches(PDF, "Report.Pdf"));
			assertFalse(EgovFileSignatureUtil.matches(SCRIPT, "EVIL.PNG"));
		}

		@Test
		@DisplayName("시그니처 뒤에 어떤 내용이 와도 앞부분만 본다")
		void 앞부분만_판정() {
			byte[] longPdf = new byte[4096];
			System.arraycopy(PDF, 0, longPdf, 0, PDF.length);
			assertTrue(EgovFileSignatureUtil.matches(longPdf, "big.pdf"));
		}
	}

	@Nested
	@DisplayName("계약의 경계 — 단독으로는 방어가 되지 않는다")
	class 계약_경계 {

		@Test
		@DisplayName("시그니처를 등록하지 않은 확장자는 내용을 보지 않고 통과시킨다")
		void 미등록_확장자는_통과() {
			assertTrue(EgovFileSignatureUtil.matches(SCRIPT, "note.txt"));
			assertTrue(EgovFileSignatureUtil.matches(SCRIPT, "data.csv"));
			assertTrue(EgovFileSignatureUtil.matches(SCRIPT, "evil.jsp"));
			assertTrue(EgovFileSignatureUtil.matches(SCRIPT, "noextension"));
		}

		@Test
		@DisplayName("검사 대상 확장자인지 미리 확인할 수 있다")
		void 검사_대상_여부() {
			assertTrue(EgovFileSignatureUtil.isSupportedExtension("photo.jpg"));
			assertTrue(EgovFileSignatureUtil.isSupportedExtension("REPORT.PDF"));
			assertFalse(EgovFileSignatureUtil.isSupportedExtension("note.txt"));
			assertFalse(EgovFileSignatureUtil.isSupportedExtension("noextension"));
			assertFalse(EgovFileSignatureUtil.isSupportedExtension(null));
		}

		@Test
		@DisplayName("컨테이너가 같은 포맷끼리는 구분되지 않는다")
		void 동일_컨테이너_한계() {
			assertTrue(EgovFileSignatureUtil.matches(ZIP, "doc.hwpx"));
			assertTrue(EgovFileSignatureUtil.matches(ZIP, "doc.docx"));
			assertTrue(EgovFileSignatureUtil.matches(ZIP, "sheet.xlsx"));
			assertTrue(EgovFileSignatureUtil.matches(CFBF, "old.doc"));
			assertTrue(EgovFileSignatureUtil.matches(CFBF, "old.xls"));
			assertEquals("zip", EgovFileSignatureUtil.detect(ZIP));
			assertEquals("cfbf", EgovFileSignatureUtil.detect(CFBF));
		}

		@Test
		@DisplayName("경로가 섞인 파일명은 마지막 구간에서만 확장자를 본다")
		void 경로_구간_정규화() {
			assertTrue(EgovFileSignatureUtil.matches(SCRIPT, "dir.zip/report"));
			assertFalse(EgovFileSignatureUtil.isSupportedExtension("dir.zip/report"));
			assertFalse(EgovFileSignatureUtil.matches(SCRIPT, "dir.txt/photo.jpg"));
			assertFalse(EgovFileSignatureUtil.matches(SCRIPT, "dir\\photo.jpg"));
		}

		@Test
		@DisplayName("등록된 확장자에 내용이 없으면 거부한다")
		void 내용_없음() {
			assertFalse(EgovFileSignatureUtil.matches((byte[]) null, "photo.jpg"));
			assertTrue(EgovFileSignatureUtil.matches((byte[]) null, "note.txt"));
		}
	}

	@Nested
	@DisplayName("스트림으로도 검사한다")
	class 스트림_검사 {

		@Test
		@DisplayName("스트림 앞부분을 읽어 판정한다")
		void 스트림_판정() throws IOException {
			assertTrue(EgovFileSignatureUtil.matches(new ByteArrayInputStream(PNG), "logo.png"));
			assertFalse(EgovFileSignatureUtil.matches(new ByteArrayInputStream(SCRIPT), "evil.png"));
		}

		@Test
		@DisplayName("헤더보다 짧은 스트림도 안전하게 판정한다")
		void 짧은_스트림() throws IOException {
			assertTrue(EgovFileSignatureUtil.matches(new ByteArrayInputStream(new byte[] { 0x42, 0x4D }), "tiny.bmp"));
			assertFalse(EgovFileSignatureUtil.matches(new ByteArrayInputStream(new byte[0]), "empty.png"));
		}

		@Test
		@DisplayName("한 번에 조금씩 주는 스트림에서도 헤더를 다 읽는다")
		void 분할_전달_스트림() throws IOException {
			InputStream oneByteAtATime = new ByteArrayInputStream(PNG) {
				@Override
				public synchronized int read(byte[] buffer, int off, int len) {
					return super.read(buffer, off, 1);
				}
			};
			assertTrue(EgovFileSignatureUtil.matches(oneByteAtATime, "logo.png"));
		}

		@Test
		@DisplayName("스트림이 null 이면 거부한다")
		void 널_스트림() throws IOException {
			assertFalse(EgovFileSignatureUtil.matches((InputStream) null, "photo.jpg"));
		}
	}

	@Nested
	@DisplayName("내용으로 포맷을 판별한다")
	class 포맷_판별 {

		@Test
		@DisplayName("등록된 포맷의 이름을 돌려준다")
		void 포맷명_반환() {
			assertEquals("pdf", EgovFileSignatureUtil.detect(PDF));
			assertEquals("jpg", EgovFileSignatureUtil.detect(JPG));
			assertEquals("png", EgovFileSignatureUtil.detect(PNG));
			assertEquals("gif", EgovFileSignatureUtil.detect(GIF));
			assertEquals("bmp", EgovFileSignatureUtil.detect(BMP));
		}

		@Test
		@DisplayName("판별하지 못하면 null 을 돌려준다")
		void 판별_불가() {
			assertNull(EgovFileSignatureUtil.detect(SCRIPT));
			assertNull(EgovFileSignatureUtil.detect(new byte[0]));
			assertNull(EgovFileSignatureUtil.detect(null));
		}

		@Test
		@DisplayName("검사 대상 확장자 목록은 바꿀 수 없다")
		void 확장자_목록() {
			Set<String> extensions = EgovFileSignatureUtil.getSupportedExtensions();
			assertTrue(extensions.contains("hwp"));
			assertTrue(extensions.contains("hwpx"));
			assertTrue(extensions.contains("jpeg"));
			assertFalse(extensions.contains("txt"));
			assertThrows(UnsupportedOperationException.class, () -> extensions.add("txt"));
		}
	}

}
