package org.egovframe.rte.fdl.excel.config;

import org.apache.ibatis.session.ExecutorType;
import org.egovframe.rte.fdl.excel.download.CategoryExcelView;
import org.egovframe.rte.fdl.excel.download.CategoryPOIExcelView;
import org.egovframe.rte.fdl.excel.impl.EgovExcelServiceImpl;
import org.egovframe.rte.fdl.excel.upload.EgovExcelBigTestMapping;
import org.egovframe.rte.psl.orm.ibatis.SqlMapClientFactoryBean;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.BeanNameViewResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Objects;

@Configuration
public class ExcelTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:/org/egovframe/rte/fdl/excel/messages/excel",
                "META-INF/message/message-common"
        );
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(60);
        return messageSource;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("META-INF/testdata/testdb.sql")
                .build();
    }

    @Bean
    public DataSourceTransactionManager txManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean(name = "sqlMapClient")
    public SqlMapClientFactoryBean sqlMapClient() {
        SqlMapClientFactoryBean factoryBean = new SqlMapClientFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setConfigLocation(new ClassPathResource("META-INF/sqlmap/sql-map-config.xml"));
        return factoryBean;
    }

    @Bean(name = "sqlSession")
    public SqlSessionFactoryBean sqlSession() throws IOException {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setConfigLocation(new ClassPathResource("META-INF/sqlmap/sql-mapper-config.xml"));
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("META-INF/sqlmap/mappings/*-mapper.xml")
        );
        return factoryBean;
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(Objects.requireNonNull(sqlSession().getObject()), ExecutorType.BATCH);
    }

    @Bean
    public EgovExcelServiceImpl excelService() throws Exception {
        EgovExcelServiceImpl service = new EgovExcelServiceImpl();
        service.setMapClass("org.egovframe.rte.fdl.excel.upload.EgovExcelTestMapping");
        service.setSqlSessionTemplate(sqlSessionTemplate());
        return service;
    }

    @Bean
    public EgovExcelServiceImpl excelBigService() {
        EgovExcelServiceImpl service = new EgovExcelServiceImpl();
        service.setMapClass("org.egovframe.rte.fdl.excel.upload.EgovExcelTestMapping");
        service.setMapBeanName("mappingBean");
        service.setSqlMapClient(sqlMapClient().getObject());
        return service;
    }

    @Bean
    public EgovExcelBigTestMapping mappingBean() {
        return new EgovExcelBigTestMapping();
    }

    @Bean
    public BeanNameViewResolver beanNameViewResolver() {
        BeanNameViewResolver beanNameViewResolver = new BeanNameViewResolver();
        beanNameViewResolver.setOrder(0);
        return beanNameViewResolver;
    }

    @Bean
    public CategoryExcelView categoryExcelView() {
        return new CategoryExcelView();
    }

    @Bean
    public CategoryPOIExcelView categoryPOIExcelView() {
        return new CategoryPOIExcelView();
    }

    @Bean
    public BeanNameViewResolver beanNameViewResolverPOIExcelView() {
        BeanNameViewResolver beanNameViewResolver = new BeanNameViewResolver();
        beanNameViewResolver.setOrder(0);
        return beanNameViewResolver;
    }

}
