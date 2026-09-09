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

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.egovframe.rte.fdl.filehandling.EgovFiles;

/**
 * 업로드 파일을 서버에 저장할 때 쓸 <b>유일한 파일명</b>을 만든다.
 *
 * <p><b>NOTE:</b> 사용자가 보낸 이름을 그대로 저장 경로에 쓰면 세 가지가 한꺼번에 걸린다 —
 * 같은 이름이 오면 <b>덮어써지고</b>, 이름에 섞인 경로·특수문자가 저장 위치를 흔들며,
 * 다른 사용자의 파일명을 <b>추측</b>할 수 있게 된다. 저장명은 새로 만들고 원본 이름은
 * 별도 컬럼에 보관하는 것이 표준적인 처리다.</p>
 *
 * <pre>
 * String stored = EgovStoredFileNames.generate(file.getOriginalFilename());
 * // 예: "0a7f3c1e9b4d42f8a1c6d5e2f7b8c9d0.pdf"
 *
 * Path target = EgovFiles.resolveSecurely(baseDir, stored);   // 경로까지 확정
 * </pre>
 *
 * <p><b>원본 이름은 애플리케이션이 보관한다.</b> 이 클래스는 저장명만 만든다 — 다운로드 시
 * 원래 이름으로 내려주려면 DB 등에 원본 이름을 함께 저장해 두었다가
 * {@code Content-Disposition} 에 실어야 한다. 한글 파일명이 깨지지 않는 헤더 값 생성은
 * {@link org.egovframe.rte.fdl.filehandling.EgovContentDispositions} 를 쓴다.</p>
 *
 * <p><b>확장자는 기본적으로 보존한다.</b> 확장자를 버리면 운영자가 파일을 알아볼 수 없고
 * 다운로드 시 형식 판별도 어려워지기 때문이다. 다만 확장자가 남아 있다는 것은
 * <b>업로드 디렉터리에서 실행 권한이 제거되어 있다는 전제</b> 위에서만 안전하다. 실행 가능한
 * 위치에 두어야 하는 사정이 있다면 {@link #generateWithoutExtension()} 을 쓴다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovFileMngUtil.getTimeStamp 의
 *                            목적(유일한 저장명)만 차용하고 구현은 재작성 — 원본은
 *                            12시간제 hh 로 오전·오후를 구분하지 못하고, 밀리초 해상도
 *                            시각만으로 이름을 만들어 동시 업로드 시 덮어써지며,
 *                            시각 기반이라 파일명이 예측 가능했다. 코드 이식 아님)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovStoredFileNames {

	/** 확장자로 인정하는 최대 길이 — 이보다 길면 확장자가 아닌 것으로 본다. */
	private static final int MAX_EXTENSION_LENGTH = 20;

	private EgovStoredFileNames() {
	}

	/**
	 * 무작위 저장명을 만든다(확장자 보존).
	 *
	 * <p>이름은 하이픈을 제거한 UUID 32자에 확장자를 붙인 형태다. 시각 정보를 담지 않으므로
	 * <b>다른 파일의 이름을 추측할 수 없다.</b></p>
	 *
	 * @param originalFileName 사용자가 보낸 원본 파일명({@code null} 허용)
	 * @return 저장용 파일명(예: {@code "0a7f...d0.pdf"})
	 */
	public static String generate(String originalFileName) {
		return generate(originalFileName, UUID.randomUUID());
	}

	/**
	 * 주어진 UUID 로 저장명을 만든다(확장자 보존).
	 *
	 * <p><b>시간순 정렬이 필요하면 UUIDv7 을 넘긴다.</b> 파일명이 생성 시각 순서로 정렬되면서도
	 * 뒤쪽 난수 때문에 예측은 되지 않는다. 실행환경은 {@code fdl.idgnr} 의
	 * {@code EgovUuidV7GnrServiceImpl.generate()} 로 UUIDv7 을 제공한다 — 본 모듈이 그 모듈을
	 * 의존하지 않으므로(파일명 생성만을 위해 JDBC 계열 의존을 끌어오지 않는다) 값을 넘겨받는 형태로 둔다.</p>
	 *
	 * <pre>
	 * String stored = EgovStoredFileNames.generate(name, EgovUuidV7GnrServiceImpl.generate());
	 * </pre>
	 *
	 * @param originalFileName 원본 파일명({@code null} 허용)
	 * @param uuid             사용할 UUID(필수)
	 * @return 저장용 파일명
	 */
	public static String generate(String originalFileName, UUID uuid) {
		Objects.requireNonNull(uuid, "uuid must not be null");
		String base = uuid.toString().replace("-", "");
		String extension = extensionOf(originalFileName);
		return extension.isEmpty() ? base : base + "." + extension;
	}

	/**
	 * 확장자 없는 무작위 저장명을 만든다.
	 *
	 * <p>업로드 디렉터리를 실행 가능한 위치에 두어야 하는 등 확장자를 남길 수 없는 사정이
	 * 있을 때 쓴다. 대신 파일 형식은 애플리케이션이 따로 기록해야 한다.</p>
	 *
	 * @return 저장용 파일명(하이픈 없는 UUID 32자)
	 */
	public static String generateWithoutExtension() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 저장명에 붙일 <b>정규화된 확장자</b>를 반환한다.
	 *
	 * <p>경로를 제거하고 소문자로 바꾼 뒤, <b>ASCII 영숫자로만 이루어진 경우에만</b> 확장자로
	 * 인정한다. 공백·따옴표·널 바이트처럼 경로나 헤더를 흔들 수 있는 문자가 저장명에
	 * 섞이지 않게 하기 위해서다. 인정되지 않으면 빈 문자열을 돌려주며, 그 경우 저장명은
	 * 확장자 없이 만들어진다.</p>
	 *
	 * @param originalFileName 원본 파일명({@code null} 허용)
	 * @return 소문자 확장자(점 없음). 없거나 인정되지 않으면 빈 문자열
	 */
	public static String extensionOf(String originalFileName) {
		if (originalFileName == null) {
			return "";
		}
		String extension = EgovFiles.getExtension(originalFileName.trim()).toLowerCase(Locale.ROOT);
		if (extension.isEmpty() || extension.length() > MAX_EXTENSION_LENGTH) {
			return "";
		}
		for (int i = 0; i < extension.length(); i++) {
			char ch = extension.charAt(i);
			boolean alphaNumeric = (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9');
			if (!alphaNumeric) {
				return "";
			}
		}
		return extension;
	}
}
