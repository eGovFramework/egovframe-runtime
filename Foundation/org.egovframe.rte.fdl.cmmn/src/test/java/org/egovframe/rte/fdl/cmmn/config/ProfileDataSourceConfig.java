package org.egovframe.rte.fdl.cmmn.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.egovframe.rte.fdl.cmmn.profiles.TestProfile1;
import org.egovframe.rte.fdl.cmmn.profiles.TestProfile2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
public class ProfileDataSourceConfig {

    // 공통 DataSource
    @Bean(name = "commonDataSource")
    public DataSource commonDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl("jdbc:hsqldb:hsql://localhost/springpoc");
        ds.setUsername("poc");
        ds.setPassword("poc");
        return ds;
    }

    @Bean(name = "dataSource")
    @Profile("poc")
    public DataSource pocDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl("jdbc:hsqldb:hsql://localhost/springpoc");
        ds.setUsername("poc");
        ds.setPassword("poc");
        return ds;
    }

    @Bean
    @Profile("poc")
    public TestProfile2 tempPoc() {
        return new TestProfile2();
    }

    @Bean(name = "dataSource")
    @Profile("embedded")
    public DataSource embeddedDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:/META-INF/testdata/testdb.sql")
                .build();
    }

    @Bean
    @Profile("embedded")
    public TestProfile1 tempEmbedded() {
        return new TestProfile1();
    }

}
