package org.egovframe.rte.fdl.string;

import org.junit.After;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * 클래스 로딩 허용목록을 검증한다(CWE-470).
 */
public class EgovObjectUtilSecurityTest {

    @After
    public void tearDown() {
        EgovObjectUtil.setAllowedClassNames(null);
    }

    @Test
    public void 허용목록이_없으면_기존처럼_로딩한다() throws Exception {
        assertEquals(StringBuilder.class, EgovObjectUtil.loadClass("java.lang.StringBuilder"));
    }

    @Test
    public void 허용목록에_없는_클래스는_로딩하지_않는다() {
        EgovObjectUtil.setAllowedClassNames(Collections.singleton("java.lang.StringBuilder"));
        assertThrows(SecurityException.class, () -> EgovObjectUtil.loadClass("java.lang.ProcessBuilder"));
        assertThrows(SecurityException.class, () -> EgovObjectUtil.instantiate("java.lang.ProcessBuilder"));
    }

    @Test
    public void 허용목록의_클래스는_인스턴스화한다() {
        EgovObjectUtil.setAllowedClassNames(Collections.singleton("java.lang.StringBuilder"));
        assertEquals(StringBuilder.class, EgovObjectUtil.instantiate("java.lang.StringBuilder").getClass());
    }

}
