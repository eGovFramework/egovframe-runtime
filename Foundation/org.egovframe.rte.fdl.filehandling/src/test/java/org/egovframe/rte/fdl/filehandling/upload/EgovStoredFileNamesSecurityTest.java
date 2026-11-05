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

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * EgovStoredFileNames 의 <b>저장명 오염</b> 회귀 검증.
 *
 * <p>저장명은 결국 파일시스템 경로와 다운로드 헤더에 실린다. 공격자가 원본 파일명에 무엇을
 * 실어 보내든 저장명에는 <b>UUID 32자 + 소문자 영숫자 확장자</b> 외의 문자가 들어가지 않아야 한다.
 * 확장자 자리로 경로 구분자·인용부호·널바이트·ADS 구분자가 새어 들어오면 그 자체가 취약점이다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.07  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public class EgovStoredFileNamesSecurityTest {

	private static final String BS = String.valueOf((char) 92);

	@Test
	public void 저장명_형식_불변() {
		List<String> hostile = Arrays.asList(
				"../../etc/passwd.txt", "dir" + BS + "shell.jsp", "shell.jsp::$DATA", "a.png\"; rm -rf ~",
				"a.png'", "a.png;b", "shell.jsp" + (char) 0 + ".png", "shell．jsp", "x." + new String(new char[25]).replace("\0", "p"),
				"file.PNG", "file.pNg", "file.png.", "  spaced.txt  ", "", "no-extension", ".", "..");
		for (String name : hostile) {
			String stored = EgovStoredFileNames.generate(name);
			assertTrue("형식 위반: " + name.replace("\0", "<NUL>") + " -> " + stored, stored.matches("[0-9a-f]{32}(\\.[a-z0-9]{1,20})?"));
		}
	}

	@Test
	public void 오염_확장자_탈락() {
		assertEquals("", EgovStoredFileNames.extensionOf("shell.jsp::$DATA"));
		assertEquals("", EgovStoredFileNames.extensionOf("a.png\""));
		assertEquals("", EgovStoredFileNames.extensionOf("a.png;b"));
		assertEquals("", EgovStoredFileNames.extensionOf("shell．jsp"));
		assertEquals("", EgovStoredFileNames.extensionOf("x." + new String(new char[25]).replace("\0", "p")));
		assertEquals("", EgovStoredFileNames.extensionOf("file.png."));
	}

	@Test
	public void 널바이트_확장자() {
		// 정책 단계에서 이미 거부되지만, 저장명 생성 단계 단독으로도 위험 문자가 남지 않아야 한다
		String stored = EgovStoredFileNames.generate("shell.jsp" + (char) 0 + ".png");
		assertTrue(stored, stored.endsWith(".png"));
		assertFalse(stored, stored.contains("\0") || stored.contains("jsp"));
	}

	@Test
	public void 구분자_없음() {
		for (String name : Arrays.asList("../../etc/passwd.txt", "dir" + BS + "x.png", "/abs/x.png")) {
			String stored = EgovStoredFileNames.generate(name);
			assertFalse(stored, stored.contains("/") || stored.contains(BS) || stored.contains(".."));
		}
	}
}
