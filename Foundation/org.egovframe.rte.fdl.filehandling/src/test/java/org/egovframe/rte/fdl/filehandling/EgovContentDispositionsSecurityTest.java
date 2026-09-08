package org.egovframe.rte.fdl.filehandling;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovContentDispositions 의 <b>헤더 인젝션(CWE-113)</b> 회귀 검증 — 기능 테스트와 별도로
 * 공격자가 파일명에 실어 보낼 법한 입력을 모아, 어떤 입력에도 헤더 값에 CR/LF 가 남지 않고
 * quoted-string 이 닫히지 않은 채 끝나지 않음을 고정한다.
 */
public class EgovContentDispositionsSecurityTest {

    private static final String BS = String.valueOf((char) 92);

    private static final List<String> HOSTILE = List.of(
            "evil.txt\r\nSet-Cookie: pwn=1",
            "evil.txt\nX-Injected: 1",
            "evil.txt\rX-Injected: 1",
            "evil.txt\r\n\r\n<script>alert(1)</script>",
            "a\"b\"c.txt",
            "a" + BS + "\"b.txt",
            "line sep.txt",       // U+2028 LINE SEPARATOR
            "nextline.txt",      // U+0085 NEL
            "tab\tname.txt",
            "100%.txt", "it's*.txt", "semi;colon.txt", "eq=uals.txt",
            "../../etc/passwd",
            "x".repeat(2000) + ".txt");

    @Test
    public void 어떤_입력에도_헤더_값에_CR_LF가_남지_않는다() {
        for (String name : HOSTILE) {
            String header = EgovContentDispositions.attachment(name);
            assertFalse(header.contains("\r") || header.contains("\n"),
                    "CR/LF 잔존: " + name.replace("\r", "<CR>").replace("\n", "<LF>") + " -> " + header);
        }
    }

    @Test
    public void 인용부호는_항상_이스케이프되어_quoted_string이_깨지지_않는다() {
        for (String name : HOSTILE) {
            String header = EgovContentDispositions.attachment(name);
            String quoted = header.substring(header.indexOf("filename=\"") + "filename=\"".length());
            // 폴백 filename 값 안에 이스케이프되지 않은 " 가 있으면 파라미터 경계가 무너진다
            int end = quoted.indexOf("\"; filename*=");
            String value = (end >= 0) ? quoted.substring(0, end) : quoted.substring(0, quoted.lastIndexOf('"'));
            assertFalse(value.replace(BS + BS, "").replace(BS + "\"", "").contains("\""),
                    "이스케이프되지 않은 인용부호: " + header);
        }
    }

    @Test
    public void 유니코드_줄구분자는_비ASCII_경로로_퍼센트_인코딩된다() {
        String header = EgovContentDispositions.attachment("line sep.txt");
        assertTrue(header.contains("filename*=UTF-8''line%E2%80%A8sep.txt"), header);
        assertTrue(header.contains("filename=\"line_sep.txt\""), "ASCII 폴백은 _ 치환: " + header);
    }

    @Test
    public void RFC5987_attr_char_밖의_문자는_전부_인코딩된다() {
        // ' * % 는 attr-char 가 아니므로 filename* 값에 그대로 나타나면 안 된다
        String header = EgovContentDispositions.attachment("한글 it's*100%.txt");
        String star = header.substring(header.indexOf("filename*=UTF-8''") + "filename*=UTF-8''".length());
        assertFalse(star.contains("'") || star.contains("*") || star.contains(" "), star);
        assertTrue(star.contains("%27") && star.contains("%2A") && star.contains("%25"), star);
    }

    @Test
    public void 경로가_섞인_이름은_이름만_남고_상위_이동이_헤더에_실리지_않는다() {
        assertEquals("attachment; filename=\"passwd\"", EgovContentDispositions.attachment("../../etc/passwd"));
        assertEquals("attachment; filename=\"passwd\"",
                EgovContentDispositions.attachment(".." + BS + ".." + BS + "etc" + BS + "passwd"));
    }
}
