package org.egovframe.rte.ptl.mvc.cookie;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;

/**
 * {@link EgovCookies} 의 <b>헤더 주입(CWE-113)·안전 기본값</b> 회귀 검증.
 *
 * <p>기능 테스트({@link EgovCookiesTest})와 별도로, Set-Cookie 헤더를 깨뜨리거나 속성을 끼워 넣는 데
 * 실제로 쓰이는 입력을 표로 모아 두고 <b>하나라도 쿠키가 만들어지면 실패</b>하게 한다. 컨테이너
 * (Tomcat 의 RFC 6265 처리기)도 같은 문자를 거부하지만 이 빌더는 컨테이너에 기대지 않고 생성
 * 시점에 거부한다 — 테스트 응답 객체는 검증을 하지 않으므로 여기서 그 계약을 고정한다.</p>
 */
public class EgovCookiesSecurityTest {

	private static final String CR = "\r";
	private static final String LF = "\n";
	private static final String NUL = String.valueOf((char) 0);

	/** 헤더 경계·속성 경계를 무너뜨리는 입력 — 이름·값·Path·Domain 어디에 넣어도 거부돼야 한다. */
	private static final List<String> INJECTIONS = Arrays.asList(
			"x" + CR + LF + "Set-Cookie: admin=true",
			"x" + LF + "Set-Cookie: admin=true",
			"x" + CR + "Set-Cookie: admin=true",
			"x; HttpOnly=false",
			"x; Path=/",
			"x;Secure",
			"x" + NUL + "y",
			"x" + (char) 0x7F,
			"x\t; Domain=evil.example");

	private static MockHttpServletRequest request() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContextPath("/app");
		return request;
	}

	private static boolean isCookieOctet(char c) {
		return c == 0x21 || (c >= 0x23 && c <= 0x2B) || (c >= 0x2D && c <= 0x3A)
				|| (c >= 0x3C && c <= 0x5B) || (c >= 0x5D && c <= 0x7E);
	}

	private static String label(String s) {
		return s.replace(CR, "<CR>").replace(LF, "<LF>").replace(NUL, "<NUL>");
	}

	@Test
	public void 주입_문자는_어느_자리에서도_거부() {
		for (String bad : INJECTIONS) {
			assertThrows("이름: " + label(bad), IllegalArgumentException.class, () -> EgovCookies.builder(bad, "v"));
			assertThrows("값: " + label(bad), IllegalArgumentException.class, () -> EgovCookies.builder("n", bad));
			assertThrows("Path: " + label(bad), IllegalArgumentException.class, () -> EgovCookies.builder("n", "v").path(bad));
			assertThrows("Domain: " + label(bad), IllegalArgumentException.class, () -> EgovCookies.builder("n", "v").domain(bad));
		}
	}

	@Test
	public void 세미콜론은_인코딩으로도_우회_불가() {
		// 빌더 생성 자체가 거부되므로 encodeValue() 에 도달할 수 없다
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("n", "a; Secure"));
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("n", "a" + CR + LF + "Set-Cookie: b=1"));
	}

	@Test
	public void 인코딩_발급_값은_cookie_octet_뿐() {
		for (String raw : Arrays.asList("v w", "\"quoted\"", "a,b", "back" + "\\" + "slash", "한글 이름", "100%", "a+b=c&d",
				new String(new char[4096]).replace("\0", "x"), "line" + (char) 0x2028 + "sep", "emoji😀")) {
			MockHttpServletResponse response = new MockHttpServletResponse();
			ResponseCookie cookie = EgovCookies.builder("n", raw).encodeValue().addTo(request(), response);

			for (char c : cookie.getValue().toCharArray()) {
				assertTrue("cookie-octet 밖 문자 U+" + Integer.toHexString(c) + " in " + raw, isCookieOctet(c));
			}
			String header = response.getHeader("Set-Cookie");
			assertFalse(header, header.contains(CR) || header.contains(LF));

			MockHttpServletRequest next = new MockHttpServletRequest();
			next.setCookies(new Cookie("n", cookie.getValue()));
			assertEquals(Optional.of(raw), EgovCookies.decodedValue(next, "n"));
		}
	}

	@Test
	public void SameSite_규칙과_속성_주입() {
		MockHttpServletRequest plain = request();
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("n", "v").sameSite("None").build(plain));
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("n", "v").partitioned().build(plain));
		for (String bad : Arrays.asList("None; Secure", "Lax" + CR + LF + "Set-Cookie: a=b", "Strict; Domain=evil.example", "Foo")) {
			assertThrows(label(bad), IllegalArgumentException.class, () -> EgovCookies.builder("n", "v").sameSite(bad));
		}

		MockHttpServletRequest secure = request();
		secure.setSecure(true);
		ResponseCookie cookie = EgovCookies.builder("n", "v").sameSite("none").build(secure);
		assertTrue(cookie.isSecure());
		assertEquals("None", cookie.getSameSite());
	}

	@Test
	public void 변조된_인코딩은_예외가_아니라_빈값() {
		// 쿠키 값은 클라이언트가 임의로 바꿀 수 있다 — 읽기 헬퍼가 그 값 때문에 예외를 내면 요청 처리가 500 으로 끝난다
		for (String tampered : Arrays.asList("%", "%zz", "%E0%A4%A", "abc%", "%%")) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setCookies(new Cookie("n", tampered));
			assertEquals(tampered, Optional.empty(), EgovCookies.decodedValue(request, "n"));
			assertEquals("원시 값 읽기는 그대로: " + tampered, Optional.of(tampered), EgovCookies.value(request, "n"));
		}
	}

}
