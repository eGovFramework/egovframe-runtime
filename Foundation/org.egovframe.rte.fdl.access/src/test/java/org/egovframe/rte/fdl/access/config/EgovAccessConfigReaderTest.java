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
        // 터키어 로케일에서는 "id".substring(0, 1).toUpperCase()가 점 있는 'İ'(U+0130)가 되어
        // setter 이름이 "setİd"로 만들어지고, findSetter가 null을 돌려주면서 이 키가 조용히 무시된다.
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
