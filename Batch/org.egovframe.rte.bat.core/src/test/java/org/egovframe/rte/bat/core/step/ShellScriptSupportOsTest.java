package org.egovframe.rte.bat.core.step;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OS 판별 메서드 테스트.
 *
 * <p>OS 필드는 클래스 로드 시 한 번만 채워지므로, 판별 로직만 보기 위해
 * 리플렉션으로 값을 바꾼 뒤 되돌린다.</p>
 */
public class ShellScriptSupportOsTest {

    private void withOsName(String value, Runnable body) throws Exception {
        Field field = ShellScriptSupport.class.getDeclaredField("OS");
        field.setAccessible(true);
        String original = (String) field.get(null);
        try {
            field.set(null, value);
            body.run();
        } finally {
            field.set(null, original);
        }
    }

    @Test
    @DisplayName("AIX에서 isUnix()가 true를 반환한다")
    public void isUnixOnAix() throws Exception {
        withOsName("aix", () -> assertTrue(ShellScriptSupport.isUnix(),
                "os.name이 AIX면 isUnix()가 true여야 한다"));
    }

    @Test
    @DisplayName("Linux 판별은 종전과 같다")
    public void isUnixOnOthers() throws Exception {
        withOsName("linux", () -> assertTrue(ShellScriptSupport.isUnix()));
    }
}
