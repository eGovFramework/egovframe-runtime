/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Stream;

/**
 * 세션 기반 접근 제어 자동 구성(Globals.Auth=session).
 * <p>@Bean 간 의존은 메서드 파라미터로 주입받고(proxyBeanMethods = false), 기본 DataSource 생성 이후에 적용된다.</p>
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  @AutoConfiguration 전환, @Bean 메서드 파라미터 주입, mappingPath 기본값 /**(REST),
 *                        excludeList 빈 항목 무시, loginUrl·accessDeniedUrl 자동 제외
 * </pre>
 * @since 2025.06.01
 */
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(EgovAccessProperties.class)
@ConditionalOnProperty(prefix = "Globals", name = "Auth", havingValue = "session")
@Slf4j
public class EgovAccessConfiguration implements WebMvcConfigurer {

    private static final String DEFAULT_MAPPING_PATH = "/**";

    private final EgovAccessProperties properties;

    public EgovAccessConfiguration(EgovAccessProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if ("session".equals(properties.getGlobalAuthen())) {
            // 미지정 시 전체 경로(/**) : REST 방식 URL 기준
            String mappingPath = StringUtils.hasText(properties.getMappingPath()) ? properties.getMappingPath().trim() : DEFAULT_MAPPING_PATH;
            // 빈 항목은 무시하고, 로그인·접근거부 URL 은 리다이렉트 반복을 막기 위해 항상 제외
            String excludeList = properties.getExcludeList();
            List<String> excludePatterns = Stream.concat(
                            StringUtils.hasText(excludeList) ? Stream.of(excludeList.split(",")) : Stream.empty(),
                            Stream.of(properties.getLoginUrl(), properties.getAccessDeniedUrl()))
                    .filter(StringUtils::hasText).map(String::trim).distinct().toList();
            registry.addInterceptor(new EgovAccessInterceptor(properties))
                    .addPathPatterns(mappingPath)
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
    public EgovAccessDao egovAccessDao(DataSourceFactoryBean accessDataSourceFactoryBean,
                                       AuthorityUserFactoryBean authorityUserFactoryBean,
                                       RoleAndUrlFactoryBean roleAndUrlFactoryBean) {
        EgovAccessDao egovAccessDao = new EgovAccessDao();
        egovAccessDao.setDataSource(accessDataSourceFactoryBean.getObject());
        egovAccessDao.setAuthorityUserQuery(authorityUserFactoryBean.getObject());
        egovAccessDao.setRoleAndUrlQuery(roleAndUrlFactoryBean.getObject());
        return egovAccessDao;
    }

    @Bean
    public EgovAccessServiceImpl egovAccessService(EgovAccessDao egovAccessDao) {
        EgovAccessServiceImpl egovAccessService = new EgovAccessServiceImpl();
        egovAccessService.setEgovAccessDao(egovAccessDao);
        return egovAccessService;
    }

    @Bean(initMethod = "init")
    public AuthorityMapFactoryBean authorityMap(EgovAccessServiceImpl egovAccessService) {
        AuthorityMapFactoryBean authorityMapFactoryBean = new AuthorityMapFactoryBean();
        authorityMapFactoryBean.setEgovAccessService(egovAccessService);
        return authorityMapFactoryBean;
    }

    @Bean(initMethod = "init")
    public ResourceMapFactoryBean resourceMap(EgovAccessServiceImpl egovAccessService) {
        ResourceMapFactoryBean resourceMapFactoryBean = new ResourceMapFactoryBean();
        resourceMapFactoryBean.setEgovAccessService(egovAccessService);
        return resourceMapFactoryBean;
    }

    @Bean
    public AuthorityResourceMetadata authorityResourceMetadata(AuthorityMapFactoryBean authorityMap,
                                                               ResourceMapFactoryBean resourceMap,
                                                               EgovAccessServiceImpl egovAccessService) throws Exception {
        AuthorityResourceMetadata authorityResourceMetadata = new AuthorityResourceMetadata(
                authorityMap.getObject(), resourceMap.getObject());
        authorityResourceMetadata.setEgovAccessService(egovAccessService);
        return authorityResourceMetadata;
    }

}
