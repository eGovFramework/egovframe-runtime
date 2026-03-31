package org.egovframe.rte.psl.dataaccess.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.ExecutorType;
import org.egovframe.rte.psl.dataaccess.mapper.MapperConfigurer;
import org.egovframe.rte.psl.orm.ibatis.SqlMapClientFactoryBean;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "org.egovframe.rte.psl.dataaccess")
public class DataAccessTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean(name = "jdbcProperties")
    public Properties jdbcProperties() throws IOException {
        ResourcePropertySource source = new ResourcePropertySource("classpath:META-INF/spring/jdbc.properties");
        Properties props = new Properties();
        props.putAll(source.getSource());
        return props;
    }

    @Bean(name = "schemaProperties")
    public Properties schemaProperties() throws IOException {
        ResourcePropertySource source = new ResourcePropertySource("classpath:META-INF/spring/schema.properties");
        Properties props = new Properties();
        props.putAll(source.getSource());
        return props;
    }

    @Bean(name = "dataSource")
    public DataSource dataSource(@Qualifier("jdbcProperties") Properties properties) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(properties.getProperty("jdbc.driverClassName"));
        ds.setUrl(properties.getProperty("jdbc.url"));
        ds.setUsername(properties.getProperty("jdbc.username"));
        ds.setPassword(properties.getProperty("jdbc.password"));
        ds.setDefaultAutoCommit(false);
        ds.setPoolPreparedStatements(false);
        return ds;
    }

    @Bean(name = "txManager")
    public DataSourceTransactionManager txManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlMapClient")
    public SqlMapClientFactoryBean sqlMapClient(@Qualifier("dataSource") DataSource dataSource) {
        SqlMapClientFactoryBean factoryBean = new SqlMapClientFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfigLocation(new ClassPathResource("META-INF/sqlmap/sql-map-config.xml"));
        return factoryBean;
    }

    @Bean(name = "sqlSession")
    public SqlSessionFactoryBean sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws IOException {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfigLocation(new ClassPathResource("META-INF/sqlmap/sql-mapper-config.xml"));
        Resource[] mapperLocations = new PathMatchingResourcePatternResolver().getResources("classpath:META-INF/sqlmap/mappers/*.xml");
        factoryBean.setMapperLocations(mapperLocations);
        return factoryBean;
    }

    @Bean
    public MapperConfigurer mapperConfigurer() {
        MapperConfigurer configurer = new MapperConfigurer();
        configurer.setBasePackage("org.egovframe.rte.psl.dataaccess");
        return configurer;
    }

    @Bean(name = "batchSqlSessionTemplate")
    public SqlSessionTemplate batchSqlSessionTemplate(SqlSessionFactoryBean sqlSessionFactoryBean) throws Exception {
        ExecutorType executorType = ExecutorType.BATCH;
        return new SqlSessionTemplate(sqlSessionFactoryBean.getObject(), executorType);
    }

}
