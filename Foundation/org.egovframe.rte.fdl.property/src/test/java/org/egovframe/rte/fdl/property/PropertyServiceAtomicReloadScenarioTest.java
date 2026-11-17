package org.egovframe.rte.fdl.property;

import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * PropertyServiceAtomicReloadScenarioTest
 * <p>
 * <b>NOTE</b>: refreshPropertyFiles() 가 다 읽은 뒤에만 새 값을 내보내는지 확인한다.
 * 실행환경 5.x 의 같은 이름 테스트를 Java 8 · JUnit 4 로 옮겼다.
 * @author 유지보수
 * @since 2026.11.14
 * @version 4.3
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2026.11.14  유지보수        최초 생성
 *
 * </pre>
 */
public class PropertyServiceAtomicReloadScenarioTest {

    private final Path testDir = Paths.get("target", "property-service-atomic-reload-scenario", UUID.randomUUID().toString());

    /**
     * 읽다가 실패한 refresh 는 일부만 읽은 값을 내보내지 않고, 원인을 없애면 다시 읽을 수 있다
     */
    @Test
    public void failedReloadDoesNotPublishPartiallyLoadedSnapshotAndCanRecover() throws Exception {
        Path applicationFile = testDir.resolve("application.properties");
        Path databaseFile = testDir.resolve("database.properties");
        Path missingFile = testDir.resolve("missing.properties");

        write(applicationFile, "scenario.version=v1\napplication.mode=stable\nfeature.enabled=false\n");
        write(databaseFile, "datasource.url=jdbc:old\nshared.key=file-v1\n");

        EgovPropertyServiceImpl service = createPropertyService(
                orderedExtFiles(applicationFile, databaseFile),
                Collections.singletonMap("shared.key", "programmatic"));

        assertSnapshot(service, "v1", "stable", "false", "jdbc:old", "file-v1");

        write(applicationFile, "scenario.version=v2\napplication.mode=refreshed\nfeature.enabled=true\n");
        write(databaseFile, "datasource.url=jdbc:new\nshared.key=file-v2\n");

        service.refreshPropertyFiles();

        assertSnapshot(service, "v2", "refreshed", "true", "jdbc:new", "file-v2");

        write(applicationFile, "scenario.version=v3\napplication.mode=partial-candidate\nfeature.enabled=false\n");
        write(databaseFile, "datasource.url=jdbc:partial-candidate\nshared.key=file-v3\n");
        service.setExtFileName(orderedExtFiles(applicationFile, missingFile, databaseFile));

        assertThrows(RuntimeException.class, service::refreshPropertyFiles);

        assertSnapshot(service, "v2", "refreshed", "true", "jdbc:new", "file-v2");

        service.setExtFileName(orderedExtFiles(applicationFile, databaseFile));
        service.refreshPropertyFiles();

        assertSnapshot(service, "v3", "partial-candidate", "false", "jdbc:partial-candidate", "file-v3");
    }

    /**
     * refresh 하는 동안 다른 스레드가 읽어도 비었거나 빠진 값을 보지 않는다
     */
    @Test
    public void concurrentReadersDoNotObserveClearedOrMissingPropertiesDuringRefresh() throws Exception {
        Path applicationFile = testDir.resolve("concurrent-application.properties");
        Path databaseFile = testDir.resolve("concurrent-database.properties");
        writeSnapshot(applicationFile, databaseFile, "v1");
        EgovPropertyServiceImpl service = createPropertyService(
                orderedExtFiles(applicationFile, databaseFile),
                Collections.singletonMap("shared.key", "programmatic"));
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
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getStaticMessageSource().setUseCodeAsDefaultMessage(true);
        applicationContext.refresh();

        EgovPropertyServiceImpl service = new EgovPropertyServiceImpl();
        service.setApplicationContext(applicationContext);
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
        write(applicationFile, "scenario.version=" + version + "\napplication.mode=" + version + "-mode\n");
        write(databaseFile, "datasource.url=jdbc:" + version + "\nshared.key=file-" + version + "\n");
    }

    private void write(Path path, String content) throws Exception {
        Files.createDirectories(testDir);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private String toFileLocation(Path path) {
        return path.toAbsolutePath().toUri().toString();
    }

}
