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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Test;

/**
 * EgovUploadPolicy 의 <b>확장자 화이트리스트 우회</b> 회귀 검증(CWE-434).
 *
 * <p>기능 테스트({@link EgovUploadPolicyTest})와 별도로, 업로드 필터 우회에 실제로 쓰이는
 * 파일명 변형을 한 표로 모아 두고 <b>하나라도 통과하면 실패</b>하게 한다. 새 우회 기법이
 * 알려지면 이 목록에 한 줄을 더한다.</p>
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
public class EgovUploadPolicySecurityTest {

	private static final String BS = String.valueOf((char) 92);

	private static final EgovUploadPolicy POLICY = EgovUploadPolicy.builder()
			.allowExtensionList(".gif,.jpg,.jpeg,.png,.xls,.xlsx")
			.maxFileSize(1_000_000L)
			.build();

	@After
	public void resetLocale() {
		Locale.setDefault(Locale.KOREA);
	}

	@Test
	public void 확장자_우회_변형_전부_거부() {
		List<String> bypasses = Arrays.asList(
				"shell.jsp", "shell.JSP", "shell.JsP",            // 대소문자
				"shell.png.jsp",                                   // 마지막 확장자가 실행형
				"shell.jsp.",  "shell.jsp..",                      // 끝 점 — NTFS 가 잘라내는 표기
				"shell.jsp ",  "shell.jsp\t",                      // 끝 공백·탭
				"shell.jsp::$DATA", "shell.png::$DATA",            // NTFS 대체 데이터 스트림
				"shell.jsp" + (char) 0 + ".png",                   // 널바이트 잘라먹기
				"shell．jsp", "shell.jsp／",                       // 전각 점·전각 슬래시
				".htaccess", "web.config", ".user.ini",            // 서버 설정 파일
				"a.png;x.jsp", "a.png\"", "a.png'",                // 구분자·인용부호
				"x." + new String(new char[25]).replace("\0", "p"),                             // 비정상 길이 확장자
				"../shell.png", "dir" + BS + "shell.png",          // 경로가 섞인 이름은 이름 자체를 거부
				"shell.svg", "shell.html", "shell.xml");           // 화이트리스트 밖(스크립트 실행 가능 형식)

		for (String name : bypasses) {
			String label = name.replace("\0", "<NUL>").replace("\t", "<TAB>");
			assertTrue("통과하면 안 된다: " + label, POLICY.check(name, 10L).isPresent());
		}
	}

	@Test
	public void 거부_사유_분류() {
		assertEquals(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED, POLICY.check("shell.png.jsp", 10L).get());
		assertEquals(EgovUploadPolicy.Reason.NO_EXTENSION, POLICY.check("shell.jsp.", 10L).get());
		assertEquals(EgovUploadPolicy.Reason.INVALID_FILENAME, POLICY.check("shell.jsp" + (char) 0 + ".png", 10L).get());
		assertEquals(EgovUploadPolicy.Reason.INVALID_FILENAME, POLICY.check("../shell.png", 10L).get());
		assertEquals(EgovUploadPolicy.Reason.NO_EXTENSION, POLICY.check("shell．jsp", 10L).get());
		assertEquals("콜론은 드라이브·대체 데이터 스트림 구분자라 이름 자체를 거부한다", EgovUploadPolicy.Reason.INVALID_FILENAME, POLICY.check("shell.jsp::$DATA", 10L).get());
	}

	@Test
	public void 정상_파일은_허용() {
		// "shell.jsp%00.png" 은 퍼센트 인코딩을 해석하지 않으므로 그냥 png 다 — 저장명은 uuid.png 가 된다
		for (String ok : Arrays.asList("photo.png", "PHOTO.PNG", "보고서.xlsx", "a.b.c.jpg", "shell.jsp.png", "shell.jsp%00.png")) {
			assertTrue("허용돼야 한다: " + ok, !POLICY.check(ok, 10L).isPresent());
		}
	}

	@Test
	public void 로케일_무관() {
		Locale.setDefault(new Locale("tr", "TR"));
		assertTrue("대문자 I 를 포함한 확장자", !POLICY.check("IMAGE.GIF", 10L).isPresent());
		assertTrue(POLICY.check("shell.JSP", 10L).isPresent());
	}

	@Test
	public void 빈_화이트리스트는_생성_거부() {
		// 빈 목록이 "전부 거부" 로 조용히 동작하면 설정 실수가 운영까지 숨어 들어간다.
		// 빌더가 즉시 실패해야 기동 시점에 드러난다.
		assertThrows(IllegalStateException.class,
				() -> EgovUploadPolicy.builder().allowExtensionList("").maxFileSize(10L).build());
		assertThrows(IllegalStateException.class,
				() -> EgovUploadPolicy.builder().allowExtensionList("   ").maxFileSize(10L).build());
		assertThrows(IllegalStateException.class,
				() -> EgovUploadPolicy.builder().maxFileSize(10L).build());
	}
}
