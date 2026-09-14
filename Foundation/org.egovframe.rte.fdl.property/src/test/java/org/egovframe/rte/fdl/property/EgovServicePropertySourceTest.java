package org.egovframe.rte.fdl.property;

import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovPropertyService 값이 Spring Environment 에서 보이는지 검증한다.
 */
public class EgovServicePropertySourceTest {

    private EgovPropertyServiceImpl propertyService;

    @BeforeEach
    public void setUp() throws Exception {
        propertyService = new EgovPropertyServiceImpl();
        Map<String, String> props = new LinkedHashMap<>();
        props.put("Globals.DbType", "hsql");
        props.put("Globals.MainPage", "/index.do");
        props.put("Globals.PageSize", "20");
        propertyService.setProperties(props);
        propertyService.afterPropertiesSet();
    }

    @Test
    public void testReadsServiceValuesAndReturnsNullForMissingKey() {
        EgovServicePropertySource source = new EgovServicePropertySource("egovProps", propertyService);

        assertEquals("hsql", source.getProperty("Globals.DbType"));
        assertNull(source.getProperty("no.such.key"), "없는 키는 예외 대신 null 이어야 다음 PropertySource 로 넘어간다");
        List<String> names = List.of(source.getPropertyNames());
        assertTrue(names.contains("Globals.DbType") && names.contains("Globals.MainPage") && names.contains("Globals.PageSize"), names.toString());
    }

    @Test
    public void testEnvironmentResolvesPlaceholdersFromService() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new EgovServicePropertySource("egovProps", propertyService));

        assertEquals("hsql", environment.getProperty("Globals.DbType"));
        assertEquals("main=/index.do", environment.resolvePlaceholders("main=${Globals.MainPage}"));
        assertEquals("fallback", environment.resolvePlaceholders("${no.such.key:fallback}"), "없는 키는 기본값 구문으로 넘어간다");
    }

    @Test
    public void testEnvironmentConvertsTypes() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new EgovServicePropertySource("egovProps", propertyService));

        assertEquals(Integer.valueOf(20), environment.getProperty("Globals.PageSize", Integer.class));
    }

    @Test
    public void testRefreshedValuesAreVisibleThroughSameSource() throws Exception {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new EgovServicePropertySource("egovProps", propertyService));

        Map<String, String> updated = new LinkedHashMap<>();
        updated.put("Globals.DbType", "oracle");
        propertyService.setProperties(updated);
        propertyService.refreshPropertyFiles();

        assertEquals("oracle", environment.getProperty("Globals.DbType"), "다시 읽은 값이 재기동 없이 보여야 한다");
        assertNull(environment.getProperty("Globals.MainPage"), "다시 읽으면서 사라진 키는 보이지 않는다");
    }
}
