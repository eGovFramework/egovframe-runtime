package org.egovframe.rte.fdl.property.config;

import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class PropertyServiceExtendConfig {

    @Bean(name = "propertyServiceExtend", destroyMethod = "destroy")
    public EgovPropertyServiceImpl propertyService() {
        EgovPropertyServiceImpl propertyService = new EgovPropertyServiceImpl();

        Set<Object> extFileName = new LinkedHashSet<>();

        Map<String, String> map1 = new HashMap<>();
        map1.put("encoding", "UTF-8");
        map1.put("filename", "file:./src/**/refresh-resource.properties");
        extFileName.add(map1);

        extFileName.add("file:./src/**/resource.properties");

        Map<String, String> map2 = new HashMap<>();
        map2.put("encoding", "UTF-8");
        map2.put("filename", "classpath:/META-INF/properties/korean-resource.properties");
        extFileName.add(map2);

        propertyService.setExtFileName(extFileName);

        // properties 설정
        Map<String, Object> properties = new HashMap<>();
        properties.put("AAAA", "1234");
        properties.put("bbbb", "4567");
        properties.put("cccc", "가나다");

        propertyService.setProperties(properties);

        return propertyService;
    }

}
