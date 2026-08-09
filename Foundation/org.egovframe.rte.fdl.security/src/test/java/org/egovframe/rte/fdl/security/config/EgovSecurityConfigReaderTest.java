package org.egovframe.rte.fdl.security.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EgovSecurityConfigReaderTest {

    @Test
    public void testReadConfigKeepsSafeDefaultsForOmittedKeys(@TempDir Path tempDir) throws IOException {
        // loginUrl만 지정하고 sniff/xssProtection/xframeOptions는 생략한 부분 설정 파일.
        Path propsFile = tempDir.resolve("egov-security-config.properties");
        Properties props = new Properties();
        props.setProperty("loginUrl", "/custom/login.do");
        try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
            props.store(writer, null);
        }

        EgovSecurityConfigReader reader = new EgovSecurityConfigReader("file:" + propsFile, null);
        EgovSecurityConfig config = reader.readConfig();

        assertEquals("/custom/login.do", config.getLoginUrl());
        // 형제 구현체 EgovAccessConfigReader와 동일하게, 파일에 명시되지 않은 키는
        // createDefaultConfig()의 안전 기본값(sniff=true, xssProtection=true, xframeOptions=SAMEORIGIN)을 유지해야 한다.
        assertTrue(config.isSniff());
        assertTrue(config.isXssProtection());
        assertEquals("SAMEORIGIN", config.getXframeOptions());
    }

}
