package org.egovframe.rte.bat.core.reflection;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * EgovReflectionSupport 의 setter/getter 이름 생성이 JVM 기본 로케일과 무관함을 검증한다.
 *
 * <p>터키어 로케일에서는 "id".substring(0,1).toUpperCase() 가 점 있는 'İ'(U+0130) 이 되어
 * "setİd"/"getİd" 를 찾게 되고, 실제 메서드명(setId/getId)과 일치하지 않아 methodMap 에
 * null 이 담긴다. 이후 invoke 시점에 NullPointerException 으로 죽는다(CWE-176).</p>
 */
public class EgovReflectionSupportLocaleTest {

    private static final String[] NAMES = {"id"};

    /** 필드명이 i 로 시작하는 VO. 터키어 로케일에서 대문자화가 깨지는 경계 케이스다. */
    public static class LocaleVo {
        private String id;

        public LocaleVo() {
        }

        public LocaleVo(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @Test
    public void getterMethodMapResolvesUnderTurkishLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("egov", readIdUnderCurrentLocale(),
                    "터키어 로케일에서도 getId 를 찾아 값을 읽어야 한다");

            Locale.setDefault(Locale.ENGLISH);
            assertEquals("egov", readIdUnderCurrentLocale(),
                    "영어 로케일에서도 동일해야 한다");
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void setterMethodMapResolvesUnderTurkishLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            EgovReflectionSupport<LocaleVo> support = new EgovReflectionSupport<LocaleVo>();
            support.generateSetterMethodMap(LocaleVo.class, NAMES);

            assertNotNull(support.getMethodMap().get("id"),
                    "터키어 로케일에서도 setId 를 찾아야 한다");
        } finally {
            Locale.setDefault(original);
        }
    }

    /** getter map 을 만들고 id 값을 읽는다. 수정 전에는 map 에 null 이 담겨 invoke 에서 NPE 가 난다. */
    private Object readIdUnderCurrentLocale() {
        EgovReflectionSupport<LocaleVo> support = new EgovReflectionSupport<LocaleVo>();
        LocaleVo item = new LocaleVo("egov");
        support.generateGetterMethodMap(NAMES, item);
        return support.invokeGettterMethod(item, "id");
    }
}
