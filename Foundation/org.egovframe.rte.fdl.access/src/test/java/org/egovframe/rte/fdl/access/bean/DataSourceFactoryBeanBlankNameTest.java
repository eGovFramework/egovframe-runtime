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
package org.egovframe.rte.fdl.access.bean;

import org.apache.commons.dbcp2.BasicDataSource;
import org.egovframe.rte.fdl.access.config.EgovAccessConfig;
import org.egovframe.rte.fdl.access.config.EgovAccessConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * egov-access-config.properties 의 dataSource 를 값 없이 둔 설정에 대한 테스트
 */
public class DataSourceFactoryBeanBlankNameTest {

    @Configuration
    static class BeanOnlyContext {
        @Bean(name = "dataSource")
        public DataSource dataSource() {
            return new SimpleDriverDataSource();
        }

        @Bean
        public EgovAccessConfig egovAccessConfig() {
            EgovAccessConfig config = new EgovAccessConfig();
            config.setDataSource("");
            return config;
        }
    }

    @Configuration
    static class StartupContext {
        @Bean
        public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean(name = "dataSource")
        public DataSource dataSource() {
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.hsqldb.jdbcDriver");
            ds.setUrl("jdbc:hsqldb:mem:blankdatasourcetestdb");
            ds.setUsername("sa");
            ds.setPassword("");
            ds.setDefaultAutoCommit(true);
            try (Connection conn = ds.getConnection()) {
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("META-INF/testdata/testdb.sql"));
            } catch (Exception e) {
                throw new IllegalStateException("db init failed", e);
            }
            return ds;
        }
    }

    /**
     * 설정의 dataSource 가 빈 문자열이면 기본 dataSource bean 으로 폴백한다.
     */
    @Test
    public void blankDataSourceNameFallsBackToDefaultBean() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BeanOnlyContext.class)) {
            DataSourceFactoryBean factory = new DataSourceFactoryBean();
            factory.setApplicationContext(ctx);

            DataSource dataSource = assertDoesNotThrow(factory::getObject);
            assertSame(ctx.getBean("dataSource"), dataSource);
        }
    }

    /**
     * dataSource 를 값 없이 둔 설정 파일로도 EgovAccessConfiguration 이 기동한다.
     */
    @Test
    public void contextStartsWithBlankDataSourceName() throws Exception {
        File configFile = File.createTempFile("egov-access-config", ".properties");
        configFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("id=egovAccessConfig\n");
            writer.write("globalAuthen=session\n");
            writer.write("mappingPath=/**/*.do\n");
            writer.write("dataSource=\n");
            writer.write("loginUrl=/uat/uia/egovLoginUsr.do\n");
            writer.write("accessDeniedUrl=/uat/uia/egovLoginUsr.do?auth_error=1\n");
            writer.write("requestMatcherType=ant\n");
            writer.write("sqlAuthorityUser=SELECT USER_ID AS userid, AUTHOR_CODE AS authority FROM AUTHORITIES\n");
            writer.write("sqlRoleAndUrl=SELECT r.ROLE_PTTRN AS url, a.AUTHOR_CODE AS authority FROM ROLES r INNER JOIN AUTHROLES a ON r.ROLE_CODE = a.ROLE_CODE ORDER BY r.ROLE_SORT\n");
        }

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("blankDataSource",
                Collections.singletonMap("Globals.AccessConfigPath", "file:" + configFile.getAbsolutePath())));
        ctx.register(StartupContext.class, EgovAccessConfiguration.class);
        try {
            assertDoesNotThrow(ctx::refresh);
            assertEquals("", ctx.getBean(EgovAccessConfig.class).getDataSource());
            assertSame(ctx.getBean("dataSource"), ctx.getBean("accessDataSourceFactoryBean"));
        } finally {
            ctx.close();
        }
    }
}
