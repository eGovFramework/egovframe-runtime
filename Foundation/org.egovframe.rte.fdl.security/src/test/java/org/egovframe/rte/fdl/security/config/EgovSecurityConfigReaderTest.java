package org.egovframe.rte.fdl.security.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EgovSecurityConfigReaderTest {

    @Test
    public void testReadConfigKeepsSafeDefaultsForOmittedKeys(@TempDir Path tempDir) throws IOException {
        // loginUrl만 지정, sniff/xssProtection/xframeOptions는 생략한 부분 설정 파일
        Path propsFile = tempDir.resolve("egov-security-config.properties");
        Properties props = new Properties();
        props.setProperty("loginUrl", "/custom/login.do");
        try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
            props.store(writer, null);
        }

        EgovSecurityConfigReader reader = new EgovSecurityConfigReader("file:" + propsFile, null);
        EgovSecurityConfig config = reader.readConfig();

        assertEquals("/custom/login.do", config.getLoginUrl());
        // 미명시 키는 createDefaultConfig() 안전 기본값 유지 (sniff=true, xssProtection=true, xframeOptions=SAMEORIGIN)
        assertTrue(config.isSniff());
        assertTrue(config.isXssProtection());
        assertEquals("SAMEORIGIN", config.getXframeOptions());
    }

    @Test
    public void testReadConfigHonorsExplicitOverrideOfDefaultValue(@TempDir Path tempDir) throws IOException {
        // 명시적 sniff=false 는 안전 기본값(true)보다 우선
        Path propsFile = tempDir.resolve("egov-security-config.properties");
        Properties props = new Properties();
        props.setProperty("sniff", "false");
        try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
            props.store(writer, null);
        }

        EgovSecurityConfigReader reader = new EgovSecurityConfigReader("file:" + propsFile, null);
        EgovSecurityConfig config = reader.readConfig();

        assertFalse(config.isSniff());
        // 미명시 키는 안전 기본값 유지
        assertTrue(config.isXssProtection());
    }

    @Test
    public void testReadConfigAppliesKeysUnderTurkishLocale(@TempDir Path tempDir) throws IOException {
        // 터키어 로케일 : "id" 첫 글자 대문자화가 'İ'(U+0130) → setter "setİd" 미발견으로 키 무시
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            Path propsFile = tempDir.resolve("egov-security-config.properties");
            Properties props = new Properties();
            props.setProperty("id", "customConfigId");
            try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
                props.store(writer, null);
            }

            EgovSecurityConfigReader reader = new EgovSecurityConfigReader("file:" + propsFile, null);
            EgovSecurityConfig config = reader.readConfig();

            assertEquals("customConfigId", config.getId(),
                    "터키어 로케일에서도 properties의 id 키가 반영되어야 한다(기본값 egovSecurityConfig로 남으면 안 된다)");
        } finally {
            Locale.setDefault(original);
        }
    }


    @Test
    public void testDefaultConfigUsesBCryptHashWhenFileMissing(@TempDir Path tempDir) {
        // 설정 파일 없음 : createDefaultConfig() 적용, 기본 hash 는 bcrypt (이전 : MD5)
        EgovSecurityConfigReader reader = new EgovSecurityConfigReader("file:" + tempDir.resolve("none.properties"), null);
        EgovSecurityConfig config = reader.readConfig();
        assertEquals("bcrypt", config.getHash());
    }

    @Test
    public void testReadConfigWithoutHashKeyKeepsBCryptDefault(@TempDir Path tempDir) throws IOException {
        Path propsFile = tempDir.resolve("egov-security-config.properties");
        Files.writeString(propsFile, "loginUrl=/login.do\n", StandardCharsets.UTF_8);
        EgovSecurityConfig config = new EgovSecurityConfigReader("file:" + propsFile, null).readConfig();
        assertEquals("bcrypt", config.getHash());
    }
}
