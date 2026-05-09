package org.egovframe.rte.fdl.property;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.property.config.PropertyServiceExtendConfig;
import org.egovframe.rte.fdl.property.config.PropertyTestConfig;
import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * PropertyServiceRefreshTest
 * <b>NOTE</b>: Property Service 리로딩 기능 확인.
 *
 * @author 실행환경 개발팀 김태호
 * @version 1.0
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2009.02.01  김태호          최초 생성
 * @since 2009.02.01
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PropertyTestConfig.class, PropertyServiceExtendConfig.class})
public class PropertyServiceRefreshTest {

    @Resource(name = "propertyServiceExtend")
    private EgovPropertyService propertyService;

    private final Path testDir = Path.of("target", "property-service-refresh-test", UUID.randomUUID().toString());

    @Test
    public void testRefreshPropertiesFiles() throws FdlException, IOException {
        for (String value : propertyService.getStringArray("tokens_on_multiple_lines")) {
            System.out.println("### tokens_on_multiple_lines >>> " + value);
        }

        assertEquals(4, propertyService.getStringArray("tokens_on_multiple_lines").length);
        assertEquals("first token refresh", propertyService.getString("tokens_on_multiple_lines"));
        assertEquals(Double.valueOf(1234), Double.valueOf(propertyService.getDouble("number.double")));
        propertyService.refreshPropertyFiles();
        assertEquals("first token refresh", propertyService.getString("tokens_on_multiple_lines"));
        assertEquals(Double.valueOf(1234), Double.valueOf(propertyService.getDouble("number.double")));
    }

    @Test
    public void refreshPropertyFilesReloadsChangedFile() throws Exception {
        Path propertyFile = writeProperties("reload.properties", "reload.key=old\n");
        EgovPropertyServiceImpl service = createPropertyService(Set.of(toFileLocation(propertyFile)), null);

        assertEquals("old", service.getString("reload.key"));

        Files.writeString(propertyFile, "reload.key=new\n", StandardCharsets.UTF_8);
        service.refreshPropertyFiles();

        assertEquals("new", service.getString("reload.key"));
    }

    @Test
    public void refreshPropertyFilesPreservesExistingPropertiesWhenReloadFails() throws Exception {
        Path propertyFile = writeProperties("stable.properties", "stable.key=old\n");
        Path missingFile = testDir.resolve("missing.properties");
        Set<String> extFileName = new LinkedHashSet<>();
        extFileName.add(toFileLocation(propertyFile));
        extFileName.add(toFileLocation(missingFile));
        EgovPropertyServiceImpl service = createPropertyService(Set.of(toFileLocation(propertyFile)), null);

        Files.writeString(propertyFile, "stable.key=new\n", StandardCharsets.UTF_8);
        service.setExtFileName(extFileName);

        assertThrows(RuntimeException.class, service::refreshPropertyFiles);
        assertEquals("old", service.getString("stable.key"));
    }

    @Test
    public void refreshPropertyFilesAppliesProgrammaticPropertiesAfterFileProperties() throws Exception {
        Path propertyFile = writeProperties("override.properties", "override.key=file\n");
        EgovPropertyServiceImpl service = createPropertyService(
                Set.of(toFileLocation(propertyFile)),
                Map.of("override.key", "programmatic"));

        assertArrayEquals(new String[]{"file", "programmatic"}, service.getStringArray("override.key"));

        Files.writeString(propertyFile, "override.key=file-refresh\n", StandardCharsets.UTF_8);
        service.refreshPropertyFiles();

        assertArrayEquals(new String[]{"file-refresh", "programmatic"}, service.getStringArray("override.key"));
    }

    private Path writeProperties(String fileName, String content) throws IOException {
        Files.createDirectories(testDir);
        Path propertyFile = testDir.resolve(fileName);
        Files.writeString(propertyFile, content, StandardCharsets.UTF_8);
        return propertyFile;
    }

    private String toFileLocation(Path path) {
        return path.toAbsolutePath().toUri().toString();
    }

    private EgovPropertyServiceImpl createPropertyService(Set<?> extFileName, Map<?, ?> properties) throws Exception {
        EgovPropertyServiceImpl service = new EgovPropertyServiceImpl();
        service.setResourceLoader(new PathMatchingResourcePatternResolver());
        service.setExtFileName(extFileName);
        service.setProperties(properties);
        service.afterPropertiesSet();
        return service;
    }

}
