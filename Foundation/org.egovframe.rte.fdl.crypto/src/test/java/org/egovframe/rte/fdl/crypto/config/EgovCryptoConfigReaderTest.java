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
        // cryptoPropertyLocation만 지정하고 crypto/algorithm/cryptoBlockSize는 생략한 부분 설정 파일.
        Path propsFile = tempDir.resolve("egov-crypto-config.properties");
        Properties props = new Properties();
        props.setProperty("cryptoPropertyLocation", "classpath:/custom.properties");
        try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
            props.store(writer, null);
        }

        EgovCryptoConfigReader reader = new EgovCryptoConfigReader("file:" + propsFile, null);
        EgovCryptoConfig config = reader.readConfig();

        assertEquals("classpath:/custom.properties", config.getCryptoPropertyLocation());
        // 형제 구현체 EgovAccessConfigReader·EgovSecurityConfigReader와 동일하게, 파일에 명시되지 않은
        // 키는 createDefaultConfig()의 기본값(crypto=true, algorithm=SHA-256, cryptoBlockSize=1024)을 유지해야 한다.
        assertTrue(config.isCrypto());
        assertEquals("SHA-256", config.getAlgorithm());
        assertEquals(1024, config.getCryptoBlockSize());
    }

    @Test
    public void testReadConfigHonorsExplicitOverrideOfDefaultValue(@TempDir Path tempDir) throws IOException {
        // 사용자가 crypto를 명시적으로 false로 끈 경우, 기본값(true)에 밀려서는 안 된다.
        Path propsFile = tempDir.resolve("egov-crypto-config.properties");
        Properties props = new Properties();
        props.setProperty("crypto", "false");
        try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
            props.store(writer, null);
        }

        EgovCryptoConfigReader reader = new EgovCryptoConfigReader("file:" + propsFile, null);
        EgovCryptoConfig config = reader.readConfig();

        assertFalse(config.isCrypto());
        // 명시 안 한 다른 키는 여전히 기본값을 유지해야 한다.
        assertEquals("SHA-256", config.getAlgorithm());
    }

    @Test
    public void testReadConfigAppliesKeysUnderTurkishLocale(@TempDir Path tempDir) throws IOException {
        // 터키어 로케일에서는 "id".substring(0, 1).toUpperCase()가 점 있는 'İ'(U+0130)가 되어
        // setter 이름이 "setİd"로 만들어지고, findSetter가 null을 돌려주면서 이 키가 조용히 무시된다.
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
