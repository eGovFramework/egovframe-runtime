package org.egovframe.rte.fdl.string;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 식별자 변환 메서드들이 JVM 기본 로케일과 무관하게 동작하는지 검증한다.
 *
 * <p>터키어·아제르바이잔어 로케일에서는 인자 없는 toUpperCase()/toLowerCase()가
 * i ↔ İ(U+0130) / I ↔ ı(U+0131)로 변환한다. 같은 저장소의 CamelCaseUtil은
 * Character 단위 변환을 써서 이 문제가 없다.</p>
 */
public class EgovStringUtilLocaleTest {

    private void underTurkishLocale(Runnable body) {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            body.run();
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    @DisplayName("convertToCamelCase는 로케일과 무관하게 ASCII 규칙으로 변환한다")
    public void convertToCamelCaseIsLocaleIndependent() {
        underTurkishLocale(() ->
                assertEquals("printStatus", EgovStringUtil.convertToCamelCase("PRINT_STATUS", '_')));
        assertEquals("printStatus", EgovStringUtil.convertToCamelCase("PRINT_STATUS", '_'));
    }

    @Test
    @DisplayName("convertToUnderScore는 로케일과 무관하게 ASCII 규칙으로 변환한다")
    public void convertToUnderScoreIsLocaleIndependent() {
        underTurkishLocale(() ->
                assertEquals("print_id", EgovStringUtil.convertToUnderScore("printId")));
        assertEquals("print_id", EgovStringUtil.convertToUnderScore("printId"));
    }
}
