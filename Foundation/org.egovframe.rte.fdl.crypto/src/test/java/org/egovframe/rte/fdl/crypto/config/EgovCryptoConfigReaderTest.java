package org.egovframe.rte.fdl.crypto.config;

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

public class EgovCryptoConfigReaderTest {

    @Test
    public void testReadConfigKeepsSafeDefaultsForOmittedKeys(@TempDir Path tempDir) throws IOException {
        // cryptoPropertyLocation만 지정, crypto/algorithm/cryptoBlockSize는 생략한 부분 설정 파일
        Path propsFile = tempDir.resolve("egov-crypto-config.properties");
        Properties props = new Properties();
        props.setProperty("cryptoPropertyLocation", "classpath:/custom.properties");
        try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
            props.store(writer, null);
        }

        EgovCryptoConfigReader reader = new EgovCryptoConfigReader("file:" + propsFile, null);
        EgovCryptoConfig config = reader.readConfig();

        assertEquals("classpath:/custom.properties", config.getCryptoPropertyLocation());
        // 미지정 키는 createDefaultConfig() 기본값 유지 (crypto=true, algorithm=SHA-256, cryptoBlockSize=1024)
        assertTrue(config.isCrypto());
        assertEquals("SHA-256", config.getAlgorithm());
        assertEquals(1024, config.getCryptoBlockSize());
    }

    @Test
    public void testReadConfigHonorsExplicitOverrideOfDefaultValue(@TempDir Path tempDir) throws IOException {
        // crypto=false 명시 시 기본값(true)으로 덮어쓰지 않음
        Path propsFile = tempDir.resolve("egov-crypto-config.properties");
        Properties props = new Properties();
        props.setProperty("crypto", "false");
        try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
            props.store(writer, null);
        }

        EgovCryptoConfigReader reader = new EgovCryptoConfigReader("file:" + propsFile, null);
        EgovCryptoConfig config = reader.readConfig();

        assertFalse(config.isCrypto());
        // 명시하지 않은 다른 키는 기본값 유지
        assertEquals("SHA-256", config.getAlgorithm());
    }

    @Test
    public void testReadConfigAppliesKeysUnderTurkishLocale(@TempDir Path tempDir) throws IOException {
        // 터키어 로케일 : "id" 대문자화 시 'İ'(U+0130) → "setİd" 로 setter 미발견, 키 무시 방지
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            Path propsFile = tempDir.resolve("egov-crypto-config.properties");
            Properties props = new Properties();
            props.setProperty("id", "customConfigId");
            try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
                props.store(writer, null);
            }

            EgovCryptoConfigReader reader = new EgovCryptoConfigReader("file:" + propsFile, null);
            EgovCryptoConfig config = reader.readConfig();

            assertEquals("customConfigId", config.getId(),
                    "터키어 로케일에서도 properties의 id 키가 반영되어야 한다(기본값 egovCryptoConfig로 남으면 안 된다)");
        } finally {
            Locale.setDefault(original);
        }
    }

}
