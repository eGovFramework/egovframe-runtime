package org.egovframe.rte.fdl.property.config;

import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class PropertyServiceConfig {

    @Bean(name = "propertyService", destroyMethod = "destroy")
    public EgovPropertyServiceImpl propertyService() {
        EgovPropertyServiceImpl propertyService = new EgovPropertyServiceImpl();

        Map<String, String> fileEntry = new HashMap<>();
        fileEntry.put("encoding", "UTF-8");
        fileEntry.put("filename", "classpath:/META-INF/properties/korean-resource.properties");

        Set<Map<String, String>> extFileNameSet = new HashSet<>();
        extFileNameSet.add(fileEntry);

        propertyService.setExtFileName(extFileNameSet);

        return propertyService;
    }

}
