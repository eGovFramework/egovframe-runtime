package org.egovframe.rte.fdl.logging.config;

import org.egovframe.rte.fdl.logging.db.EgovConnectionFactory;
import org.egovframe.rte.fdl.logging.sample.aop.MethodParameterLoggingAspect;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:/META-INF/spring/jdbc.properties")
@ComponentScan(basePackages = "org.egovframe.rte.fdl.logging")
@EnableAspectJAutoProxy
public class LoggingTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:/META-INF/testdata/testdb.sql")
                .build();
    }

    @Bean
    public DataSourceTransactionManager txManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public EgovConnectionFactory egovConnectionFactory() {
        EgovConnectionFactory factory = new EgovConnectionFactory();
        factory.setDataSource(dataSource());
        return factory;
    }

    @Bean
    public MethodParameterLoggingAspect methodParameterLoggingAspect() {
        return new MethodParameterLoggingAspect();
    }

}
