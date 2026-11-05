package org.egovframe.rte.bat.core.step;

import org.junit.After;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertThrows;

/**
 * 셸 명령 실행 시 메타문자 차단·실행파일 허용목록을 검증한다(명령 삽입 방지, CWE-78).
 */
public class ShellScriptSupportSecurityTest {

    @After
    public void tearDown() {
        ShellScriptSupport.setAllowedExecutables(null);
    }

    @Test
    public void 셸_메타문자가_있으면_실행하지_않는다() {
        for (String command : new String[]{"ls; rm -rf /tmp/x", "ls | sh", "ls && id", "echo `id`", "echo $(id)",
                "cat < /etc/passwd", "ls > out", "ls\nid"}) {
            assertThrows(command, SecurityException.class,
                    () -> ShellScriptSupport.shellCmd(command, ShellScriptSupport.getShellResultEncoding()));
        }
    }

    @Test
    public void 허용목록에_없는_실행파일은_거부한다() {
        ShellScriptSupport.setAllowedExecutables(Collections.singleton("batchTool"));
        assertThrows(SecurityException.class,
                () -> ShellScriptSupport.shellCmd("/usr/bin/curl http://example.com", "UTF-8"));
    }

    @Test
    public void 빈_명령은_거부한다() {
        assertThrows(IllegalArgumentException.class, () -> ShellScriptSupport.shellCmd("", "UTF-8"));
        assertThrows(IllegalArgumentException.class, () -> ShellScriptSupport.shellCmd(null, "UTF-8"));
    }

}
