/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.boot.access;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.access.bean.*;
import org.egovframe.boot.access.interceptor.EgovAccessInterceptor;
import org.egovframe.boot.access.service.impl.EgovAccessDao;
import org.egovframe.boot.access.service.impl.EgovAccessServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableConfigurationProperties(EgovAccessProperties.class)
@ConditionalOnProperty(prefix="Globals", name="auth", havingValue="session")
@Slf4j
public class EgovAccessConfiguration implements WebMvcConfigurer {

    private final EgovAccessProperties properties;

    public EgovAccessConfiguration(EgovAccessProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if ("session".equals(properties.getGlobalAuthen())) {
            List<String> excludePatterns = Arrays.asList(
                    Stream.of(properties.getExcludeList().split(",")).map(String::trim).toArray(String[]::new));
            registry.addInterceptor(new EgovAccessInterceptor(properties))
                    .addPathPatterns(properties.getMappingPath())
                    .excludePathPatterns(excludePatterns);
        } else {
            log.error("### EgovAccessConfiguration Invalid settings... ");
        }
    }

    @Bean
    public DataSourceFactoryBean accessDataSourceFactoryBean() {
        return new DataSourceFactoryBean(properties);
    }

    @Bean
    public AuthorityUserFactoryBean authorityUserFactoryBean() {
        return new AuthorityUserFactoryBean(properties);
    }

    @Bean
    public RoleAndUrlFactoryBean roleAndUrlFactoryBean() {
        return new RoleAndUrlFactoryBean(properties);
    }

    @Bean
    public EgovAccessDao egovAccessDao() {
        EgovAccessDao egovAccessDao = new EgovAccessDao();
        egovAccessDao.setDataSource(accessDataSourceFactoryBean().getObject());
        egovAccessDao.setAuthorityUserQuery(authorityUserFactoryBean().getObject());
        egovAccessDao.setRoleAndUrlQuery(roleAndUrlFactoryBean().getObject());
        return egovAccessDao;
    }

    @Bean
    public EgovAccessServiceImpl egovAccessService() {
        EgovAccessServiceImpl egovAccessService = new EgovAccessServiceImpl();
        egovAccessService.setEgovAccessDao(egovAccessDao());
        return egovAccessService;
    }

    @Bean(initMethod = "init")
    public AuthorityMapFactoryBean authorityMap() {
        AuthorityMapFactoryBean authorityMapFactoryBean = new AuthorityMapFactoryBean();
        authorityMapFactoryBean.setEgovAccessService(egovAccessService());
        return authorityMapFactoryBean;
    }

    @Bean(initMethod = "init")
    public ResourceMapFactoryBean resourceMap() {
        ResourceMapFactoryBean resourceMapFactoryBean = new ResourceMapFactoryBean();
        resourceMapFactoryBean.setEgovAccessService(egovAccessService());
        return resourceMapFactoryBean;
    }

    @Bean
    public AuthorityResourceMetadata authorityResourceMetadata() throws Exception {
        AuthorityResourceMetadata authorityResourceMetadata = new AuthorityResourceMetadata(
                authorityMap().getObject(), resourceMap().getObject());
        authorityResourceMetadata.setEgovAccessService(egovAccessService());
        return authorityResourceMetadata;
    }

}
