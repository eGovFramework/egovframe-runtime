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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 파일 앞부분의 시그니처(매직넘버)로 선언된 확장자와 실제 내용의 일치 여부를 검사한다.
 * 시그니처가 등록되지 않은 확장자는 통과시키므로 {@link EgovUploadPolicy} 화이트리스트와 함께 사용한다.
 * ZIP 계열(hwpx·docx 등)과 CFBF 계열(hwp·doc 등)은 컨테이너가 같아 서로 구분하지 않는다.
 *
 * <pre>
 * policy.validate(fileName, fileSize);
 * boolean ok = EgovFileSignatureUtil.matches(file.getBytes(), fileName);
 * </pre>
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
 * @since 4.3.1
 */
public final class EgovFileSignatureUtil {

	/** 시그니처 판별에 필요한 헤더 최대 바이트 수. */
	private static final int HEADER_LENGTH = 8;

	private static final byte[] SIG_PDF = { 0x25, 0x50, 0x44, 0x46 };
	private static final byte[] SIG_JPG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
	private static final byte[] SIG_PNG = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
	private static final byte[] SIG_GIF = { 0x47, 0x49, 0x46, 0x38 };
	private static final byte[] SIG_BMP = { 0x42, 0x4D };
	private static final byte[] SIG_ZIP = { 0x50, 0x4B, 0x03, 0x04 };
	private static final byte[] SIG_CFBF = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1,
			0x1A, (byte) 0xE1 };

	/** 확장자(소문자)별 허용 시그니처 목록(불변). */
	private static final Map<String, List<byte[]>> EXT_SIGNATURES;

	static {
		Map<String, List<byte[]>> table = new HashMap<>();
		register(table, "pdf", SIG_PDF);
		register(table, "jpg", SIG_JPG);
		register(table, "jpeg", SIG_JPG);
		register(table, "png", SIG_PNG);
		register(table, "gif", SIG_GIF);
		register(table, "bmp", SIG_BMP);
		register(table, "zip", SIG_ZIP);
		register(table, "hwpx", SIG_ZIP);
		register(table, "docx", SIG_ZIP);
		register(table, "xlsx", SIG_ZIP);
		register(table, "pptx", SIG_ZIP);
		register(table, "jar", SIG_ZIP);
		register(table, "hwp", SIG_CFBF);
		register(table, "doc", SIG_CFBF);
		register(table, "xls", SIG_CFBF);
		register(table, "ppt", SIG_CFBF);
		EXT_SIGNATURES = Collections.unmodifiableMap(table);
	}

	private EgovFileSignatureUtil() {
	}

	private static void register(Map<String, List<byte[]>> table, String extension, byte[]... signatures) {
		table.computeIfAbsent(extension, key -> new ArrayList<>()).addAll(Arrays.asList(signatures));
	}

	/**
	 * 파일 내용이 선언된 확장자의 시그니처와 맞는지 본다.
	 *
	 * @param content  파일 내용(앞부분 8바이트면 충분하다)
	 * @param fileName 확장자를 포함한 파일명
	 * @return 시그니처가 맞거나 검사 대상 확장자가 아니면 {@code true}
	 */
	public static boolean matches(byte[] content, String fileName) {
		List<byte[]> signatures = EXT_SIGNATURES.get(extensionOf(fileName));
		if (signatures == null) {
			return true;
		}
		if (content == null) {
			return false;
		}
		for (byte[] signature : signatures) {
			if (startsWith(content, signature)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 스트림 앞부분을 읽어 선언된 확장자의 시그니처와 맞는지 본다. 읽은 만큼 스트림 위치가 이동한다.
	 *
	 * @param in       파일 입력 스트림
	 * @param fileName 확장자를 포함한 파일명
	 * @return {@link #matches(byte[], String)} 과 같다. {@code in} 이 {@code null} 이면 {@code false}
	 * @throws IOException 스트림을 읽지 못한 경우
	 */
	public static boolean matches(InputStream in, String fileName) throws IOException {
		if (in == null) {
			return false;
		}
		byte[] header = new byte[HEADER_LENGTH];
		int read = 0;
		int count;
		while (read < HEADER_LENGTH && (count = in.read(header, read, HEADER_LENGTH - read)) != -1) {
			read += count;
		}
		return matches(read == HEADER_LENGTH ? header : Arrays.copyOf(header, read), fileName);
	}

	/**
	 * 내용의 시그니처로 판별한 포맷 이름을 돌려준다.
	 *
	 * @param content 파일 내용
	 * @return {@code "pdf"}·{@code "jpg"}·{@code "png"}·{@code "gif"}·{@code "bmp"}·{@code "zip"}·{@code "cfbf"} 중 하나,
	 *         판별하지 못하면 {@code null}
	 */
	public static String detect(byte[] content) {
		if (content == null) {
			return null;
		}
		if (startsWith(content, SIG_PDF)) {
			return "pdf";
		}
		if (startsWith(content, SIG_JPG)) {
			return "jpg";
		}
		if (startsWith(content, SIG_PNG)) {
			return "png";
		}
		if (startsWith(content, SIG_GIF)) {
			return "gif";
		}
		if (startsWith(content, SIG_BMP)) {
			return "bmp";
		}
		if (startsWith(content, SIG_ZIP)) {
			return "zip";
		}
		if (startsWith(content, SIG_CFBF)) {
			return "cfbf";
		}
		return null;
	}

	/**
	 * 파일명의 확장자가 시그니처 검사 대상인지 반환한다.
	 *
	 * @param fileName 확장자를 포함한 파일명
	 * @return 등록된 시그니처가 있으면 {@code true}
	 */
	public static boolean isSupportedExtension(String fileName) {
		return EXT_SIGNATURES.containsKey(extensionOf(fileName));
	}

	/**
	 * 시그니처가 등록된 확장자 목록을 돌려준다(소문자, 수정 불가).
	 *
	 * @return 검사 대상 확장자 집합
	 */
	public static Set<String> getSupportedExtensions() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(EXT_SIGNATURES.keySet()));
	}

	private static boolean startsWith(byte[] content, byte[] prefix) {
		if (content.length < prefix.length) {
			return false;
		}
		for (int i = 0; i < prefix.length; i++) {
			if (content[i] != prefix[i]) {
				return false;
			}
		}
		return true;
	}

	/** 파일명의 마지막 경로 구간에서 소문자 확장자를 추출한다. */
	private static String extensionOf(String fileName) {
		if (fileName == null) {
			return "";
		}
		int separator = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		String name = (separator < 0) ? fileName : fileName.substring(separator + 1);
		int dot = name.lastIndexOf('.');
		if (dot < 0 || dot == name.length() - 1) {
			return "";
		}
		return name.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

}
