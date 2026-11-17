package org.egovframe.rte.fdl.security.config;

import org.junit.Test;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * egov-security 가 생성하는 http 설정에서 보안 헤더가 개별 제어되는지 검증한다.
 */
public class EgovSecurityHttpSecurityTest {

    @Test
    public void sniff_해제가_기본_보안_헤더_전체를_끄지_않는다() throws IOException {
        String xml = http(false, "SAMEORIGIN", false);
        assertTrue(xml, xml.contains("<headers defaults-disabled=\"false\">"));
        assertFalse(xml, xml.contains("defaults-disabled=\"true\""));
        assertTrue(xml, xml.contains("<content-type-options disabled=\"true\"/>"));
    }

    @Test
    public void sniff_사용시_X_Content_Type_Options_를_켠다() throws IOException {
        String xml = http(true, "DENY", false);
        assertTrue(xml, xml.contains("<content-type-options disabled=\"false\"/>"));
    }

    @Test
    public void X_Frame_Options_는_허용_값만_XML_에_싣는다() throws IOException {
        assertTrue(http(true, "sameorigin", false).contains("<frame-options policy=\"SAMEORIGIN\"/>"));
        assertTrue(http(true, "DISABLE", false).contains("<frame-options disabled=\"true\"/>"));
        String injected = http(true, "DENY\"/><beans:bean class=\"evil.Bean\"", false);
        assertTrue(injected, injected.contains("<frame-options policy=\"DENY\"/>"));
        assertFalse(injected, injected.contains("evil.Bean"));
    }

    @Test
    public void 로그인_실패는_일반화_처리기를_거친다() throws IOException {
        String xml = http(true, "DENY", false);
        assertTrue(xml, xml.contains("authentication-failure-handler-ref=\"egovLoginFailHandler\""));
        assertTrue(xml, xml.contains("org.egovframe.rte.fdl.security.intercept.EgovLoginFailHandler"));
    }

    private static String http(boolean sniff, String xFrameOptions, boolean cacheControl) throws IOException {
        return StreamUtils.copyToString(EgovSecurityHttp.getInstance()
                .getHttp(false, sniff, xFrameOptions, true, cacheControl, false)
                .getInputStream(), StandardCharsets.UTF_8);
    }

}
