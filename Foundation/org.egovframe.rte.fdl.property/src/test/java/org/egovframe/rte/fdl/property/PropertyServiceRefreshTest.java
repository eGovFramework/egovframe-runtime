package org.egovframe.rte.fdl.property;

import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * PropertyServiceRefreshTest
 * <p>
 * <b>NOTE</b>: Property Service 리로딩 기능 확인.
 *
 * @author 실행환경 개발팀 김태호
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.02.01  김태호          최초 생성
 *   2026.11.14  유지보수        refresh 뒤 값이 쌓이지 않는지, 바뀐 파일 값 반영, 실패 시 기존 값 유지, properties 재적용 확인 추가(실행환경 5.x 와 같은 판정),
 *                               외부 파일 없이 properties 만 둔 경우의 refresh 확인 추가
 *
 * </pre>
 * @since 2009.02.01
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:/spring/context-common.xml",
        "classpath*:/spring/context-properties.xml"
})
public class PropertyServiceRefreshTest {

    @Resource(name = "propertyService")
    protected EgovPropertyService propertyService;

    @Autowired
    private ApplicationContext applicationContext;

    private final Path testDir = Paths.get("target", "property-service-refresh-test", UUID.randomUUID().toString());

    /**
     * 시스템 재가동없이 Property를 리로드
     *
     * @throws Exception fail to test
     */
    @Test
    public void testRefreshPropertiesFiles() throws IOException, FdlException {

        for (String value : propertyService.getStringArray("tokens_on_multiple_lines")) {
            System.out.println("##### tokens_on_multiple_lines >>> " + value);
        }

        assertEquals(4, propertyService.getStringArray("tokens_on_multiple_lines").length);
        assertEquals("first token refresh", propertyService.getString("tokens_on_multiple_lines"));
        assertEquals(Double.valueOf(1234), Double.valueOf(propertyService.getDouble("number.double")));
        propertyService.refreshPropertyFiles();
        assertEquals("first token refresh", propertyService.getString("tokens_on_multiple_lines"));
        assertEquals(Double.valueOf(1234), Double.valueOf(propertyService.getDouble("number.double")));
        // 다시 읽어도 값이 쌓이지 않는다
        assertEquals(4, propertyService.getStringArray("tokens_on_multiple_lines").length);
    }

    @Test
    public void refreshPropertyFilesReloadsChangedFile() throws Exception {
        Path propertyFile = writeProperties("reload.properties", "reload.key=old\n");
        EgovPropertyServiceImpl service = createPropertyService(Collections.singleton(toFileLocation(propertyFile)), null);

        assertEquals("old", service.getString("reload.key"));

        Files.write(propertyFile, "reload.key=new\n".getBytes(StandardCharsets.UTF_8));
        service.refreshPropertyFiles();

        assertEquals("new", service.getString("reload.key"));

        // 한 번 더 읽어도 값이 쌓이지 않는다
        service.refreshPropertyFiles();
        assertArrayEquals(new String[]{"new"}, service.getStringArray("reload.key"));
    }

    @Test
    public void refreshPropertyFilesPreservesExistingPropertiesWhenReloadFails() throws Exception {
        Path propertyFile = writeProperties("stable.properties", "stable.key=old\n");
        Path missingFile = testDir.resolve("missing.properties");
        Set<String> extFileName = new LinkedHashSet<>();
        extFileName.add(toFileLocation(propertyFile));
        extFileName.add(toFileLocation(missingFile));
        EgovPropertyServiceImpl service = createPropertyService(Collections.singleton(toFileLocation(propertyFile)), null);

        Files.write(propertyFile, "stable.key=new\n".getBytes(StandardCharsets.UTF_8));
        service.setExtFileName(extFileName);

        assertThrows(RuntimeException.class, service::refreshPropertyFiles);
        assertEquals("old", service.getString("stable.key"));
        assertArrayEquals(new String[]{"old"}, service.getStringArray("stable.key"));
    }

    @Test
    public void refreshPropertyFilesAppliesProgrammaticPropertiesAfterFileProperties() throws Exception {
        Path propertyFile = writeProperties("override.properties", "override.key=file\n");
        EgovPropertyServiceImpl service = createPropertyService(
                Collections.singleton(toFileLocation(propertyFile)),
                Collections.singletonMap("override.key", "programmatic"));

        assertArrayEquals(new String[]{"file", "programmatic"}, service.getStringArray("override.key"));

        Files.write(propertyFile, "override.key=file-refresh\n".getBytes(StandardCharsets.UTF_8));
        service.refreshPropertyFiles();

        assertArrayEquals(new String[]{"file-refresh", "programmatic"}, service.getStringArray("override.key"));
    }

    /**
     * 외부 파일 없이 properties 만 둔 빈(공통컴포넌트의 propertiesService 와 같은 구성)도 refresh 할 수 있고 값이 그대로다
     */
    @Test
    public void refreshPropertyFilesWithoutExternalFiles() throws Exception {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("pageUnit", "10");
        properties.put("pageSize", "10");
        EgovPropertyServiceImpl service = createPropertyService(null, properties);

        service.refreshPropertyFiles();

        assertArrayEquals(new String[]{"10"}, service.getStringArray("pageUnit"));
        assertArrayEquals(new String[]{"10"}, service.getStringArray("pageSize"));
    }

    private Path writeProperties(String fileName, String content) throws IOException {
        Files.createDirectories(testDir);
        Path propertyFile = testDir.resolve(fileName);
        Files.write(propertyFile, content.getBytes(StandardCharsets.UTF_8));
        return propertyFile;
    }

    private String toFileLocation(Path path) {
        return path.toAbsolutePath().toUri().toString();
    }

    private EgovPropertyServiceImpl createPropertyService(Set<?> extFileName, Map<?, ?> properties) throws Exception {
        EgovPropertyServiceImpl service = new EgovPropertyServiceImpl();
        service.setApplicationContext(applicationContext);
        service.setResourceLoader(new PathMatchingResourcePatternResolver());
        service.setExtFileName(extFileName);
        service.setProperties(properties);
        service.afterPropertiesSet();
        return service;
    }

}
