package org.egovframe.rte.fdl.access.config;

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

public class EgovAccessConfigReaderTest {

    @Test
    public void testReadConfigAppliesKeysUnderTurkishLocale(@TempDir Path tempDir) throws IOException {
        // 터키어 로케일 : "id" 대문자화 시 'İ'(U+0130) → "setİd" 로 setter 미발견, 키 무시 방지
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            Path propsFile = tempDir.resolve("egov-access-config.properties");
            Properties props = new Properties();
            props.setProperty("id", "customConfigId");
            try (BufferedWriter writer = Files.newBufferedWriter(propsFile, StandardCharsets.UTF_8)) {
                props.store(writer, null);
            }

            EgovAccessConfigReader reader = new EgovAccessConfigReader("file:" + propsFile, null);
            EgovAccessConfig config = reader.readConfig();

            assertEquals("customConfigId", config.getId(),
                    "터키어 로케일에서도 properties의 id 키가 반영되어야 한다(기본값 egovAccessConfig로 남으면 안 된다)");
        } finally {
            Locale.setDefault(original);
        }
    }

}
