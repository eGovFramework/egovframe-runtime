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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EgovPropertyServiceImpl이 atomic 하게 reload 되는지를 검증
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

    @Test
    public void concurrentReadersDoNotObserveClearedOrMissingPropertiesDuringRefresh() throws Exception {
        Path applicationFile = testDir.resolve("concurrent-application.properties");
        Path databaseFile = testDir.resolve("concurrent-database.properties");
        writeSnapshot(applicationFile, databaseFile, "v1");
        EgovPropertyServiceImpl service = createPropertyService(
                orderedExtFiles(applicationFile, databaseFile),
                Map.of("shared.key", "programmatic"));
        CountDownLatch start = new CountDownLatch(1);
        AtomicBoolean reading = new AtomicBoolean(true);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> reader = executorService.submit(() -> {
            try {
                start.await(5, TimeUnit.SECONDS);
                while (reading.get()) {
                    assertPublishedPropertiesPresent(service);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        try {
            start.countDown();
            for (int i = 0; i < 50; i++) {
                String version = i % 2 == 0 ? "v2" : "v1";
                writeSnapshot(applicationFile, databaseFile, version);
                service.refreshPropertyFiles();
            }

            reading.set(false);
            reader.get(5, TimeUnit.SECONDS);
        } finally {
            reading.set(false);
            executorService.shutdownNow();
        }
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

    private void assertPublishedPropertiesPresent(EgovPropertyService propertyService) {
        String version = propertyService.getString("scenario.version");
        String mode = propertyService.getString("application.mode");
        String datasourceUrl = propertyService.getString("datasource.url");
        String[] sharedValues = propertyService.getStringArray("shared.key");

        assertTrue("v1".equals(version) || "v2".equals(version));
        assertTrue("v1-mode".equals(mode) || "v2-mode".equals(mode));
        assertTrue("jdbc:v1".equals(datasourceUrl) || "jdbc:v2".equals(datasourceUrl));
        assertEquals(2, sharedValues.length);
        assertTrue("file-v1".equals(sharedValues[0]) || "file-v2".equals(sharedValues[0]));
        assertEquals("programmatic", sharedValues[1]);
    }

    private void writeSnapshot(Path applicationFile, Path databaseFile, String version) throws Exception {
        write(applicationFile, """
                scenario.version=%s
                application.mode=%s-mode
                """.formatted(version, version));
        write(databaseFile, """
                datasource.url=jdbc:%s
                shared.key=file-%s
                """.formatted(version, version));
    }

    private void write(Path path, String content) throws Exception {
        Files.createDirectories(testDir);
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private String toFileLocation(Path path) {
        return path.toAbsolutePath().toUri().toString();
    }

}
