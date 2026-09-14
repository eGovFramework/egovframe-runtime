package org.egovframe.rte.fdl.property;

import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 외부 프로퍼티 리소스를 읽지 못했을 때 어느 리소스가 실패했는지 예외만으로 알 수 있는지 검증한다.
 */
public class PropertyServiceLoadFailureTest {

    private static final String MISSING_CLASSPATH = "classpath:/META-INF/properties/does-not-exist.properties";

    @Test
    public void testMissingClasspathResourceReportsResourceInMessage() {
        EgovPropertyServiceImpl service = newService(Collections.singleton(MISSING_CLASSPATH));

        IllegalStateException e = assertThrows(IllegalStateException.class, service::afterPropertiesSet);

        assertTrue(e.getMessage().contains("does-not-exist.properties"),
                "실패한 리소스가 메시지에 있어야 한다: " + e.getMessage());
        assertInstanceOf(IOException.class, e.getCause(), "원인 예외를 그대로 보존해야 한다");
    }

    @Test
    public void testMissingResourceGivenAsMapEntryReportsResourceInMessage() {
        Map<String, String> entry = new HashMap<>();
        entry.put("encoding", "UTF-8");
        entry.put("filename", "file:./target/does-not-exist-either.properties");
        EgovPropertyServiceImpl service = newService(Collections.singleton(entry));

        IllegalStateException e = assertThrows(IllegalStateException.class, service::afterPropertiesSet);

        assertTrue(e.getMessage().contains("does-not-exist-either.properties"),
                "실패한 리소스가 메시지에 있어야 한다: " + e.getMessage());
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    public void testExistingResourceStillLoads() throws Exception {
        EgovPropertyServiceImpl service = newService(Collections.singleton("classpath:/META-INF/properties/resource.properties"));

        service.afterPropertiesSet();

        assertEquals("value", service.getString("key"));
    }

    private static EgovPropertyServiceImpl newService(Set<?> extFileName) {
        EgovPropertyServiceImpl service = new EgovPropertyServiceImpl();
        service.setResourceLoader(new PathMatchingResourcePatternResolver());
        service.setExtFileName(extFileName);
        return service;
    }
}
