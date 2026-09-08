package org.egovframe.rte.fdl.filehandling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovContentDispositions(RFC 6266/5987 헤더 값 생성) 검증.
 */
public class EgovContentDispositionsTest {

    @Test
    public void ASCII_파일명은_기존_형식을_유지한다() {
        assertEquals("attachment; filename=\"report.pdf\"", EgovContentDispositions.attachment("report.pdf"));
        assertEquals("inline; filename=\"chart.png\"", EgovContentDispositions.inline("chart.png"));
    }

    @Test
    public void 한글_파일명은_filename스타에_ASCII_폴백을_병기한다() {
        // 보(EB B3 B4)·고(EA B3 A0)·서(EC 84 9C) — attr-char 인 '.' 과 확장자는 그대로 남는다
        assertEquals("attachment; filename=\"___.hwp\"; filename*=UTF-8''%EB%B3%B4%EA%B3%A0%EC%84%9C.hwp",
                EgovContentDispositions.attachment("보고서.hwp"));
    }

    @Test
    public void CR_LF_등_제어문자는_제거된다_헤더_인젝션_차단() {
        String header = EgovContentDispositions.attachment("evil\r\nSet-Cookie: x=1.txt");
        assertFalse(header.contains("\r"), header);
        assertFalse(header.contains("\n"), header);
        assertEquals("attachment; filename=\"evilSet-Cookie: x=1.txt\"", header);
    }

    @Test
    public void 경로는_잘리고_파일명만_실린다() {
        assertEquals("attachment; filename=\"passwd\"", EgovContentDispositions.attachment("../../etc/passwd"));
        String windows = EgovContentDispositions.attachment("C:\\upload\\비밀문서.hwp");
        assertFalse(windows.contains("upload"), windows);
        assertTrue(windows.endsWith("filename*=UTF-8''%EB%B9%84%EB%B0%80%EB%AC%B8%EC%84%9C.hwp"), windows);
    }

    @Test
    public void 인용부호와_역슬래시는_quoted_string_규칙으로_이스케이프한다() {
        assertEquals("attachment; filename=\"a\\\"b.txt\"", EgovContentDispositions.attachment("a\"b.txt"));
    }

    @Test
    public void 공백_괄호는_폴백에_남고_filename스타에서는_퍼센트_인코딩된다() {
        assertEquals("attachment; filename=\"__ __ (2026).xlsx\""
                        + "; filename*=UTF-8''%EC%97%B0%EB%A7%90%20%EC%A0%95%EC%82%B0%20%282026%29.xlsx",
                EgovContentDispositions.attachment("연말 정산 (2026).xlsx"));
    }

    @Test
    public void null_빈값_정리후_잔여없음은_예외다() {
        assertThrows(IllegalArgumentException.class, () -> EgovContentDispositions.attachment(null));
        assertThrows(IllegalArgumentException.class, () -> EgovContentDispositions.attachment(""));
        assertThrows(IllegalArgumentException.class, () -> EgovContentDispositions.attachment("\r\n"));
    }

    @Test
    public void 확장자만_ASCII인_통상_사례_폴백은_확장자를_보존한다() {
        String header = EgovContentDispositions.attachment("연차보고서.xls");
        assertNotNull(header);
        assertTrue(header.contains("filename=\"_____.xls\""), "폴백에서 확장자 보존: " + header);
    }

}
