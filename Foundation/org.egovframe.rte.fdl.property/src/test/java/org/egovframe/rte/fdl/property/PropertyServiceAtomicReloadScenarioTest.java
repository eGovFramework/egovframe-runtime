package org.egovframe.rte.fdl.property;

import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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
 * EgovPropertyServiceImpl이 atomic 하게 reload 되는지를 검증합니다.
 */
public class PropertyServiceAtomicReloadScenarioTest {

    private final Path testDir = Path.of("target", "property-service-atomic-reload-scenario", UUID.randomUUID().toString());

    @Test
    public void failedReloadDoesNotPublishPartiallyLoadedSnapshotAndCanRecover() throws Exception {
        Path applicationFile = testDir.resolve("application.properties");
        Path databaseFile = testDir.resolve("database.properties");
        Path missingFile = testDir.resolve("missing.properties");

        write(applicationFile, """
                scenario.version=v1
                application.mode=stable
                feature.enabled=false
                """);
        write(databaseFile, """
                datasource.url=jdbc:old
                shared.key=file-v1
                """);

        EgovPropertyServiceImpl service = createPropertyService(
                orderedExtFiles(applicationFile, databaseFile),
                Map.of("shared.key", "programmatic"));

        assertSnapshot(service, "v1", "stable", "false", "jdbc:old", "file-v1");

        write(applicationFile, """
                scenario.version=v2
                application.mode=refreshed
                feature.enabled=true
                """);
        write(databaseFile, """
                datasource.url=jdbc:new
                shared.key=file-v2
                """);

        service.refreshPropertyFiles();

        assertSnapshot(service, "v2", "refreshed", "true", "jdbc:new", "file-v2");

        write(applicationFile, """
                scenario.version=v3
                application.mode=partial-candidate
                feature.enabled=false
                """);
        write(databaseFile, """
                datasource.url=jdbc:partial-candidate
                shared.key=file-v3
                """);
        service.setExtFileName(orderedExtFiles(applicationFile, missingFile, databaseFile));

        assertThrows(RuntimeException.class, service::refreshPropertyFiles);

        assertSnapshot(service, "v2", "refreshed", "true", "jdbc:new", "file-v2");

        service.setExtFileName(orderedExtFiles(applicationFile, databaseFile));
        service.refreshPropertyFiles();

        assertSnapshot(service, "v3", "partial-candidate", "false", "jdbc:partial-candidate", "file-v3");
    }

    private EgovPropertyServiceImpl createPropertyService(Set<?> extFileName, Map<?, ?> properties) throws Exception {
        EgovPropertyServiceImpl service = new EgovPropertyServiceImpl();
        service.setResourceLoader(new PathMatchingResourcePatternResolver());
        service.setExtFileName(extFileName);
        service.setProperties(properties);
        service.afterPropertiesSet();
        return service;
    }

    private Set<String> orderedExtFiles(Path... paths) {
        Set<String> extFileName = new LinkedHashSet<>();
        for (Path path : paths) {
            extFileName.add(toFileLocation(path));
        }
        return extFileName;
    }

    private void assertSnapshot(
            EgovPropertyService propertyService,
            String version,
            String mode,
            String featureEnabled,
            String datasourceUrl,
            String sharedFileValue) {
        assertEquals(version, propertyService.getString("scenario.version"));
        assertEquals(mode, propertyService.getString("application.mode"));
        assertEquals(featureEnabled, propertyService.getString("feature.enabled"));
        assertEquals(datasourceUrl, propertyService.getString("datasource.url"));
        assertArrayEquals(new String[]{sharedFileValue, "programmatic"}, propertyService.getStringArray("shared.key"));
    }

    private void write(Path path, String content) throws Exception {
        Files.createDirectories(testDir);
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private String toFileLocation(Path path) {
        return path.toAbsolutePath().toUri().toString();
    }

}
