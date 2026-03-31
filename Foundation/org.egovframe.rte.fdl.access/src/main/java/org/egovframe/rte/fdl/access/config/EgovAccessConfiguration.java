/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.fdl.access.config;

import org.egovframe.rte.fdl.access.bean.*;
import org.egovframe.rte.fdl.access.service.impl.EgovAccessDao;
import org.egovframe.rte.fdl.access.service.impl.EgovAccessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * egov-access Java Configuration 클래스
 *
 * <p>Desc.: access-config.xml을 대체하는 Java Configuration</p>
 *
 * @author 유지보수
 * @version 6.0
 * @since 2025.06.01
 */
@Configuration
public class EgovAccessConfiguration {

    @Value("${Globals.AccessConfigPath:}")
    private String accessConfigPath;

    @Autowired
    private ApplicationContext applicationContext;

    private EgovAccessConfig accessConfig;

    /**
     * 설정 파일 리더 Bean. Globals.AccessConfigPath는 서비스의 globals.properties에서 읽으며,
     * ApplicationContext를 ResourceLoader로 전달하여 서비스 classpath/리소스 기준으로 파일을 찾는다.
     */
    @Bean
    public EgovAccessConfigReader egovAccessConfigReader(
            @Value("${Globals.AccessConfigPath:}") String accessConfigPath) {
        return new EgovAccessConfigReader(accessConfigPath, applicationContext);
    }

    @Bean
    public EgovAccessConfig egovAccessConfig(@Value("${Globals.AccessConfigPath:}") String accessConfigPath) {
        if (accessConfig == null) {
            EgovAccessConfigReader reader = new EgovAccessConfigReader(accessConfigPath, applicationContext);
            accessConfig = reader.readConfig();
        }
        return accessConfig;
    }

    @Bean
    public DataSourceFactoryBean accessDataSourceFactoryBean() {
        DataSourceFactoryBean factory = new DataSourceFactoryBean();
        factory.setApplicationContext(applicationContext);
        return factory;
    }

    @Bean
    public AuthorityUserFactoryBean authorityUserFactoryBean() {
        AuthorityUserFactoryBean factory = new AuthorityUserFactoryBean();
        factory.setApplicationContext(applicationContext);
        return factory;
    }

    @Bean
    public RoleAndUrlFactoryBean roleAndUrlFactoryBean() {
        RoleAndUrlFactoryBean factory = new RoleAndUrlFactoryBean();
        factory.setApplicationContext(applicationContext);
        return factory;
    }

    @Bean
    public EgovAccessDao egovAccessDao(EgovAccessConfig accessConfig) throws Exception {
        EgovAccessDao dao = new EgovAccessDao();
        dao.setApplicationContext(applicationContext);
        dao.setDataSource(accessDataSourceFactoryBean().getObject());
        dao.setAuthorityUserQuery(authorityUserFactoryBean().getObject());
        dao.setRoleAndUrlQuery(roleAndUrlFactoryBean().getObject());
        return dao;
    }

    @Bean
    public EgovAccessServiceImpl egovAccessService(EgovAccessConfig accessConfig) throws Exception {
        EgovAccessServiceImpl service = new EgovAccessServiceImpl();
        service.setEgovAccessDao(egovAccessDao(accessConfig));
        return service;
    }

    @Bean
    public AuthorityMapFactoryBean authorityMap(EgovAccessConfig accessConfig) throws Exception {
        AuthorityMapFactoryBean factory = new AuthorityMapFactoryBean();
        factory.setEgovAccessService(egovAccessService(accessConfig));
        factory.init();
        return factory;
    }

    @Bean
    public ResourceMapFactoryBean resourceMap(EgovAccessConfig accessConfig) throws Exception {
        ResourceMapFactoryBean factory = new ResourceMapFactoryBean();
        factory.setEgovAccessService(egovAccessService(accessConfig));
        factory.init();
        return factory;
    }

    @Bean
    public AuthorityResourceMetadata authorityResource(EgovAccessConfig accessConfig) throws Exception {
        List<Map<String, Object>> authorityList = authorityMap(accessConfig).getObject();
        List<Map<String, Object>> resourceList = resourceMap(accessConfig).getObject();

        AuthorityResourceMetadata metadata = new AuthorityResourceMetadata(authorityList, resourceList);
        metadata.setEgovAccessService(egovAccessService(accessConfig));
        return metadata;
    }

    /**
     * RequestMappingHandlerMapping 빈에 EgovAccessInterceptor를 주입하는 BeanPostProcessor
     */
    @Bean
    public EgovAccessHandlerMappingPostProcessor egovAccessHandlerMappingPostProcessor(EgovAccessConfig egovAccessConfig) {
        return new EgovAccessHandlerMappingPostProcessor(egovAccessConfig);
    }

}
