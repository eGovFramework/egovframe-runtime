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
 * 업로드 파일의 서버 저장용 무작위 파일명(UUID)을 생성한다.
 * 원본 이름은 저장 경로에 쓰지 않고 애플리케이션이 별도로 보관한다. 확장자는 기본 보존한다.
 *
 * <pre>
 * String stored = EgovStoredFileNames.generate(file.getOriginalFilename());   // 예: "0a7f...d0.pdf"
 * Path target = EgovFiles.resolveSecurely(baseDir, stored);
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
public final class EgovStoredFileNames {

	/** 확장자로 인정하는 최대 길이. */
	private static final int MAX_EXTENSION_LENGTH = 20;

	private EgovStoredFileNames() {
	}

	/**
	 * 무작위 저장명(하이픈 없는 UUID 32자 + 확장자)을 만든다.
	 *
	 * @param originalFileName 사용자가 보낸 원본 파일명({@code null} 허용)
	 * @return 저장용 파일명(예: {@code "0a7f...d0.pdf"})
	 */
	public static String generate(String originalFileName) {
		return generate(originalFileName, UUID.randomUUID());
	}

	/**
	 * 주어진 UUID로 저장명을 만든다(확장자 보존). 시간순 정렬이 필요하면 UUIDv7을 넘긴다.
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
	 * @return 저장용 파일명(하이픈 없는 UUID 32자)
	 */
	public static String generateWithoutExtension() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 저장명에 붙일 소문자 확장자를 반환한다. ASCII 영숫자로만 이루어진 경우에만 인정한다.
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
