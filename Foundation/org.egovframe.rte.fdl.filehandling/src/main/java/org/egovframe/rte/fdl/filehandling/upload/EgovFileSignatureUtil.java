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
 * 파일 앞부분의 <b>시그니처(매직넘버)</b>를 읽어 선언된 확장자와 실제 내용이 맞는지 본다.
 *
 * <p><b>NOTE:</b> 확장자는 사용자가 지어 보내는 문자열일 뿐이다. {@link EgovUploadPolicy} 의 화이트리스트는
 * "이름이 {@code .jpg} 인가"를 볼 뿐 내용 바이트를 읽지 않으므로, 실행 가능한 파일을 {@code .jpg} 로 바꿔
 * 올리는 위장을 이름만으로는 가려낼 수 없다. 이 유틸이 그 한 겹을 맡는다. 한국 공공에서 쓰는
 * HWP(CFBF)·HWPX(ZIP) 등 사무 문서 포맷을 함께 등록해 두었다.</p>
 *
 * <pre>
 * // 업로드 처리 — 이름 검사(정책)와 내용 검사(시그니처)를 함께 둔다
 * policy.validate(fileName, fileSize);
 * if (!EgovFileSignatureUtil.matches(file.getBytes(), fileName)) {
 *     throw new EgovUploadRejectedException(fileName, EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED);
 * }
 *
 * EgovFileSignatureUtil.detect(header);   // 내용으로 본 포맷: "pdf"/"jpg"/... 판별 불가면 null
 * </pre>
 *
 * <p><b>시그니처를 등록하지 않은 확장자는 통과시킨다(이 검사에 한해 fail-open).</b> 텍스트·CSV 처럼 고정된
 * 앞부분 바이트가 없는 형식까지 이 유틸이 거부하면 쓸 수 없기 때문이다. 따라서 <b>단독으로 쓰면 방어가 되지
 * 않는다</b> — 허용할 확장자 자체는 {@link EgovUploadPolicy} 의 화이트리스트(fail-closed)가 정하고, 이 유틸은
 * 그 안에서 이름과 내용의 불일치를 잡는 순서로 써야 한다. 어떤 확장자가 실제 검사 대상인지는
 * {@link #isSupportedExtension(String)} 으로 확인할 수 있다.</p>
 *
 * <p>같은 시그니처를 여러 확장자가 나눠 쓴다. ZIP 계열({@code hwpx}·{@code docx}·{@code xlsx}·{@code pptx})과
 * 구 오피스 CFBF 계열({@code hwp}·{@code doc}·{@code xls}·{@code ppt})은 <b>컨테이너 형식이 같아</b> 서로
 * 구분되지 않는다. 컨테이너 안의 내용까지 봐야 하는 검증은 이 유틸의 범위 밖이다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.14  실행환경팀     최초 생성 (공통컴포넌트 EgovFileSignatureUtil 의 시그니처 표를
 *                            차용하고, 확장자를 파일명 마지막 경로 구간에서만 뽑도록 정규화하며
 *                            검사 대상 여부를 알려주는 isSupportedExtension ·
 *                            getSupportedExtensions 를 더했다)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
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

	/** 확장자(소문자) → 허용 시그니처 목록. 클래스 로딩 때 채우고 이후 바뀌지 않는다. */
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
	 * @return 등록된 시그니처와 맞거나 검사 대상 확장자가 아니면 {@code true}, 위·변조로 어긋나면 {@code false}
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
	 * 스트림 앞부분을 읽어 선언된 확장자의 시그니처와 맞는지 본다.
	 *
	 * <p>읽은 만큼 <b>스트림 위치가 이동한다.</b> 이어서 같은 스트림으로 파일을 저장하려면 {@code mark}/{@code reset}
	 * 을 지원하는 스트림을 쓰거나, 바이트를 먼저 읽어 두고 {@link #matches(byte[], String)} 을 쓴다.</p>
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
	 * 이 파일명의 확장자가 시그니처 검사 대상인지 알려준다.
	 *
	 * <p>{@code false} 라면 {@link #matches} 는 내용을 보지 않고 {@code true} 를 돌려준다. 업로드 정책이 허용한
	 * 확장자가 검사 대상인지 미리 확인해 검사 공백을 알아차리는 데 쓴다.</p>
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

	/**
	 * 파일명에서 확장자를 뽑는다. 경로가 섞여 들어와도 <b>마지막 구간</b>에서만 확장자를 본다 —
	 * {@code "dir.zip/report"} 의 확장자는 {@code "zip/report"} 가 아니라 없음이다.
	 */
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
